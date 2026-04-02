package com.example.PixelMageEcomerceProject.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.PixelMageEcomerceProject.dto.response.UnlinkRequestResponse;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.Card;
import com.example.PixelMageEcomerceProject.entity.UnlinkRequest;
import com.example.PixelMageEcomerceProject.enums.CardProductStatus;
import com.example.PixelMageEcomerceProject.enums.UnlinkRequestStatus;
import com.example.PixelMageEcomerceProject.exceptions.TokenExpiredException;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.CardRepository;
import com.example.PixelMageEcomerceProject.repository.UnlinkRequestRepository;
import com.example.PixelMageEcomerceProject.service.EmailService;
import com.example.PixelMageEcomerceProject.service.interfaces.NFCScanService;
import com.example.PixelMageEcomerceProject.service.interfaces.UnlinkRequestService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import com.example.PixelMageEcomerceProject.mapper.UnlinkRequestMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Business logic for UnlinkRequest flow (TASK-05).
 *
 * Key design decisions:
 * - Token: UUID, 10-minute expiry. Expired → TokenExpiredException → 410 Gone.
 * - EmailService: reused as-is — 3 new @Async methods added to existing class.
 * - approve(): delegates to nfcScanService.unlinkCard() which runs the full chain
 *   (inventory -1 → story revoke → achievement revoke). @Transactional ensures
 *   atomicity: if unlinkCard() throws, request status update rolls back too.
 * - If card no longer LINKED when Staff approves → 409 Conflict (not NPE).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnlinkRequestServiceImpl implements UnlinkRequestService {

    private final UnlinkRequestRepository unlinkRequestRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final NFCScanService nfcScanService;
    private final EmailService emailService;
    private final UnlinkRequestMapper unlinkRequestMapper;

    private static final int TOKEN_EXPIRY_MINUTES = 10;

    // ── Customer operations ───────────────────────────────────────────────────

    /**
     * Customer tạo request hủy liên kết. Gửi email verify ngay sau khi lưu.
     * @Transactional: request phải được persist trước khi email được gửi.
     */
    @Override
    @Transactional
    public UnlinkRequestResponse createRequest(Integer customerId, String nfcUid) {
        Account customer = accountRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Tài khoản không tồn tại: " + customerId));

        Card card = cardRepository.findByNfcUid(nfcUid)
                .orElseThrow(() -> new EntityNotFoundException("Thẻ không tồn tại với NFC UID: " + nfcUid));

        // Validate: card phải LINKED và thuộc về customer này
        if (!CardProductStatus.LINKED.equals(card.getStatus())) {
            throw new IllegalStateException("Thẻ chưa được liên kết — không thể tạo yêu cầu hủy liên kết.");
        }
        if (card.getOwner() == null || !card.getOwner().getCustomerId().equals(customerId)) {
            throw new IllegalStateException("Thẻ này không thuộc tài khoản của bạn.");
        }

        // Tạo UnlinkRequest với token UUID + expiry 10 phút
        UnlinkRequest request = new UnlinkRequest();
        request.setCustomer(customer);
        request.setNfcUid(nfcUid);
        request.setStatus(UnlinkRequestStatus.PENDING_EMAIL);
        request.setToken(UUID.randomUUID().toString());
        request.setTokenExpiry(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));

        unlinkRequestRepository.save(request);
        log.info("UnlinkRequest created: id={}, customerId={}, nfcUid={}", request.getId(), customerId, nfcUid);

        // Gửi email xác nhận — @Async, non-blocking, không crash flow nếu mail fail
        emailService.sendUnlinkVerificationEmail(customer.getEmail(), customer.getName(), request.getToken());

        return unlinkRequestMapper.toResponse(request);
    }

    /**
     * Customer click link trong email → trạng thái chuyển sang EMAIL_CONFIRMED.
     * Token hết hạn → TokenExpiredException (HTTP 410 Gone).
     */
    @Override
    @Transactional
    public void verifyToken(String token) {
        UnlinkRequest request = unlinkRequestRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Token không hợp lệ hoặc không tồn tại."));

        if (LocalDateTime.now().isAfter(request.getTokenExpiry())) {
            throw new TokenExpiredException(
                    "Link xác nhận đã hết hạn (10 phút). Vui lòng tạo yêu cầu mới.");
        }

        if (UnlinkRequestStatus.PENDING_EMAIL != request.getStatus()) {
            throw new IllegalStateException("Token này đã được sử dụng. Trạng thái hiện tại: " + request.getStatus());
        }

        request.setStatus(UnlinkRequestStatus.EMAIL_CONFIRMED);
        unlinkRequestRepository.save(request);
        log.info("UnlinkRequest email confirmed: id={}", request.getId());
    }

    // ── Staff operations ──────────────────────────────────────────────────────

    /**
     * Trả về danh sách request đang chờ Staff xử lý (status = EMAIL_CONFIRMED).
     */
    @Override
    public List<UnlinkRequestResponse> getQueueForStaff() {
        return unlinkRequestMapper.toResponses(unlinkRequestRepository
                .findByStatusOrderByCreatedAtDesc(UnlinkRequestStatus.EMAIL_CONFIRMED));
    }

    /**
     * Staff approve → thực thi unlinkCard() chain đầy đủ:
     *   inventory -1 → story revoke → achievement revoke.
     *
     * @Transactional: nếu unlinkCard() throws (card không còn LINKED),
     *   status update không được commit.
     */
    @Override
    @Transactional
    public void approve(Long requestId) {
        UnlinkRequest request = unlinkRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("UnlinkRequest không tồn tại: " + requestId));

        if (UnlinkRequestStatus.EMAIL_CONFIRMED != request.getStatus()) {
            throw new IllegalStateException(
                    "Chỉ có thể approve request ở trạng thái EMAIL_CONFIRMED. Trạng thái hiện tại: "
                            + request.getStatus());
        }

        // Guard: nếu card không còn LINKED → 409, không để NPE
        Card card = cardRepository.findByNfcUid(request.getNfcUid())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Thẻ không tồn tại với NFC UID: " + request.getNfcUid()));

        if (!CardProductStatus.LINKED.equals(card.getStatus())) {
            throw new IllegalStateException(
                    "Thẻ không còn ở trạng thái LINKED (có thể đã bị hủy liên kết thủ công). "
                            + "Yêu cầu không thể thực hiện.");
        }

        Integer customerId = request.getCustomer().getCustomerId();

        // Thực thi full unlinkCard() chain: inventory -1 → story revoke → achievement revoke
        nfcScanService.unlinkCard(request.getNfcUid(), customerId);
        log.info("UnlinkRequest approved: id={}, customerId={}, nfcUid={}", requestId, customerId, request.getNfcUid());

        request.setStatus(UnlinkRequestStatus.APPROVED);
        request.setResolvedAt(LocalDateTime.now());
        unlinkRequestRepository.save(request);

        // Notify customer — @Async, non-blocking
        Account customer = request.getCustomer();
        emailService.sendUnlinkApprovedEmail(customer.getEmail(), customer.getName(), request.getNfcUid());
    }

    /**
     * Staff reject → notify customer via email.
     */
    @Override
    @Transactional
    public void reject(Long requestId, String staffNote) {
        UnlinkRequest request = unlinkRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("UnlinkRequest không tồn tại: " + requestId));

        if (UnlinkRequestStatus.EMAIL_CONFIRMED != request.getStatus()) {
            throw new IllegalStateException(
                    "Chỉ có thể reject request ở trạng thái EMAIL_CONFIRMED. Trạng thái hiện tại: "
                            + request.getStatus());
        }

        request.setStatus(UnlinkRequestStatus.REJECTED);
        request.setStaffNote(staffNote);
        request.setResolvedAt(LocalDateTime.now());
        unlinkRequestRepository.save(request);
        log.info("UnlinkRequest rejected: id={}, note={}", requestId, staffNote);

        // Notify customer — @Async, non-blocking
        Account customer = request.getCustomer();
        emailService.sendUnlinkRejectedEmail(customer.getEmail(), customer.getName(), staffNote);
    }

    // ── Private helpers ───────────────────────────────────────────────────────


}
