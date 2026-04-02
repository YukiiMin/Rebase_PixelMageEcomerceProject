package com.example.PixelMageEcomerceProject.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import com.example.PixelMageEcomerceProject.dto.request.RegisterRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.UpdateProfileRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.ChangePasswordRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.ForgotPasswordRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.ResetPasswordRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.LoginRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.Role;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.RoleRepository;
import com.example.PixelMageEcomerceProject.security.service.AuthenticationService;
import com.example.PixelMageEcomerceProject.security.service.TokenService;
import com.example.PixelMageEcomerceProject.service.EmailService;
import com.example.PixelMageEcomerceProject.service.interfaces.AccountService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;
    private final TokenService tokenService;
    private final EmailService emailService;

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${google.mobile-client-id}")
    private String googleMobileClientId;

    @Override
    @Transactional
    public Account createAccount(RegisterRequestDTO dto) {
        if (existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists: " + dto.getEmail());
        }
        if (dto.getRoleName() == null) {
            dto.setRoleName("USER");
        }

        Role role = roleRepository.findByRoleName(dto.getRoleName())
                .orElseThrow(() -> new RuntimeException(
                        "Role '" + dto.getRoleName() + "' not found, Please check role name first"));

        Account newAccount = new Account();
        newAccount.setEmail(dto.getEmail());
        newAccount.setPassword(passwordEncoder.encode(dto.getPassword()));
        newAccount.setName(dto.getName());
        newAccount.setPhoneNumber(dto.getPhoneNumber());
        newAccount.setRole(role);
        newAccount.setEmailVerified(false); // Phải verify trước khi dùng

        Account saved = accountRepository.save(newAccount);

        // Tạo verification token và gửi mail — @Async nên không block response
        String verifyToken = tokenService.generateVerificationToken(saved.getEmail());
        emailService.sendVerificationEmail(saved.getEmail(), saved.getName(), verifyToken);

        return saved;
    }

    /**
     * Verify email từ token trong link mail.
     * Dùng @Query bỏ qua SQLRestriction để tìm cả account chưa verify
     * (emailVerified = false nên isEnabled() = false → SQLRestriction vẫn pass vì
     * isActive = true)
     */
    @Override
    @Transactional
    public void verifyEmail(String token) {
        String email = tokenService.consumeVerificationToken(token);
        if (email == null) {
            throw new RuntimeException("Token xác thực không hợp lệ hoặc đã hết hạn");
        }

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found: " + email));

        if (Boolean.TRUE.equals(account.getEmailVerified())) {
            return; // Đã verify rồi, idempotent — không throw
        }

        account.setEmailVerified(true);
        accountRepository.save(account);
    }

    /**
     * Resend verification email nếu user chưa nhận được
     */
    @Override
    @Transactional(readOnly = true)
    public void resendVerificationEmail(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found: " + email));

        if (Boolean.TRUE.equals(account.getEmailVerified())) {
            throw new RuntimeException("Email đã được xác thực");
        }

        String token = tokenService.generateVerificationToken(email);
        emailService.sendVerificationEmail(email, account.getName(), token);
    }

    @Override
    public Map<String, Object> loginAccount(LoginRequestDTO dto) {
        Account account = accountRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Check email verified trước khi check password (bảo mật: không lộ thông tin)
        if (!Boolean.TRUE.equals(account.getEmailVerified())) {
            throw new DisabledException("Email chưa được xác thực. Vui lòng kiểm tra hộp thư.");
        }

        if (!passwordEncoder.matches(dto.getPassword(), account.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String accessToken = authenticationService.generateToken(account);
        // Mỗi lần login tạo refresh token mới, token cũ tự bị revoke
        String refreshToken = tokenService.generateRefreshToken(account.getEmail());

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "account", account);
    }

    /**
     * Dùng refresh token để lấy access token mới mà không cần login lại
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> refreshAccessToken(String refreshToken) {
        String email = tokenService.validateRefreshToken(refreshToken);
        if (email == null) {
            throw new RuntimeException("Refresh token không hợp lệ hoặc đã hết hạn");
        }

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        String newAccessToken = authenticationService.generateToken(account);

        return Map.of("accessToken", newAccessToken);
    }

    /**
     * Logout: blacklist access token + revoke refresh token
     */
    @Override
    public void logout(String accessToken, String refreshToken, long tokenRemainingMillis) {
        tokenService.blacklistAccessToken(accessToken, tokenRemainingMillis);
        if (refreshToken != null && !refreshToken.isBlank()) {
            tokenService.revokeRefreshToken(refreshToken);
        }
    }

    @Override
    @Transactional
    public Account updateAccount(Integer customerId, UpdateProfileRequestDTO dto) {
        Account existing = accountRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + customerId));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            existing.setName(dto.getName());
        }
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()) {
            existing.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getAvatarUrl() != null && !dto.getAvatarUrl().isBlank()) {
            existing.setAvatarUrl(dto.getAvatarUrl());
        }

        return accountRepository.save(existing);
    }

    @Override
    @Transactional
    public void changePassword(Integer customerId, ChangePasswordRequestDTO dto) {
        Account account = accountRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + customerId));

        if (account.getAuthProvider() != com.example.PixelMageEcomerceProject.enums.AuthProvider.LOCAL) {
            throw new RuntimeException("Tài khoản này đăng nhập qua Google, không thể đổi mật khẩu.");
        }

        if (account.getPassword() == null || !passwordEncoder.matches(dto.getOldPassword(), account.getPassword())) {
            throw new BadCredentialsException("Mật khẩu cũ không chính xác.");
        }

        account.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequestDTO dto) {
        Account account = accountRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Account not found with email: " + dto.getEmail()));

        if (account.getAuthProvider() != com.example.PixelMageEcomerceProject.enums.AuthProvider.LOCAL) {
            throw new RuntimeException("Tài khoản này đăng nhập qua Google, không thể đặt lại mật khẩu.");
        }

        // Tạo token và gửi mail
        String token = tokenService.generateVerificationToken(account.getEmail());
        emailService.sendResetPasswordEmail(account.getEmail(), account.getName(), token);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDTO dto) {
        String email = tokenService.consumeVerificationToken(dto.getToken());
        if (email == null) {
            throw new RuntimeException("Token đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
        }

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found for email: " + email));

        account.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void deleteAccount(Integer customerId) {
        Account account = accountRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + customerId));
        account.setIsActive(false);
        accountRepository.save(account);
        // Revoke refresh token khi account bị xóa
        tokenService.revokeUserRefreshToken(account.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> getAccountById(Integer customerId) {
        return accountRepository.findById(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> getAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public Map<String, Object> verifyGoogleMobileToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new com.google.api.client.http.javanet.NetHttpTransport(),
                    new com.google.api.client.json.gson.GsonFactory())
                    .setAudience(java.util.Arrays.asList(googleClientId, googleMobileClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String googleId = payload.getSubject();
                String avatarUrl = (String) payload.get("picture");

                // Tìm hoặc tạo account mới giống như luồng web
                Account account = accountRepository.findByEmailIgnoreActive(email).orElse(null);

                if (account != null) {
                    boolean changed = false;
                    if (account.getAuthProvider() == com.example.PixelMageEcomerceProject.enums.AuthProvider.LOCAL && Boolean.TRUE.equals(account.getIsActive())) {
                        account.setAuthProvider(com.example.PixelMageEcomerceProject.enums.AuthProvider.GOOGLE);
                        account.setProviderId(googleId);
                        emailService.sendGoogleLinkedNotification(account.getEmail(), account.getName());
                        changed = true;
                        // log.info("Linked Google to existing LOCAL account from mobile verifier: {}", email);
                    } else if (!googleId.equals(account.getProviderId())) {
                        account.setProviderId(googleId);
                        changed = true;
                    }

                    if (!Boolean.TRUE.equals(account.getEmailVerified())) {
                        account.setEmailVerified(true);
                        changed = true;
                    }

                    if (account.getAvatarUrl() == null && avatarUrl != null) {
                        account.setAvatarUrl(avatarUrl);
                        changed = true;
                    }
                    if (changed) account = accountRepository.save(account);
                } else {
                    Role userRole = roleRepository.findByRoleName("USER")
                            .orElseThrow(() -> new RuntimeException("USER role not found"));
                    account = new Account();
                    account.setEmail(email);
                    account.setName(name);
                    account.setAuthProvider(com.example.PixelMageEcomerceProject.enums.AuthProvider.GOOGLE);
                    account.setProviderId(googleId);
                    account.setAvatarUrl(avatarUrl);
                    account.setRole(userRole);
                    account.setEmailVerified(true);
                    account = accountRepository.save(account);
                }

                String accessToken = authenticationService.generateToken(account);
                String refreshToken = tokenService.generateRefreshToken(account.getEmail());

                return Map.of(
                        "accessToken", accessToken,
                        "refreshToken", refreshToken,
                        "account", account);
            } else {
                throw new RuntimeException("Invalid ID token.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xác thực Google Token: " + e.getMessage(), e);
        }
    }
}
