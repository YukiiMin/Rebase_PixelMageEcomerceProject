package com.example.PixelMageEcomerceProject.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private final String displayName = "PixelMage Admin";

    /**
     * Gửi email verify account sau khi đăng ký LOCAL
     *
     * @Async để không block HTTP response — user nhận response ngay,
     *        mail gửi nền
     */
    @Async
    public void sendVerificationEmail(String toEmail, String name, String token) {
        String verifyUrl = frontendUrl + "/auth/verify?token=" + token;

        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                    <h2 style="color: #4A90E2;">Xác thực tài khoản PixelMage</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Cảm ơn bạn đã đăng ký. Vui lòng click vào nút bên dưới để xác thực email:</p>
                    <a href="%s"
                       style="display:inline-block; padding:12px 24px; background:#4A90E2;
                              color:#fff; text-decoration:none; border-radius:6px; margin:16px 0;">
                        Xác thực email
                    </a>
                    <p style="color:#888; font-size:13px;">
                        Link có hiệu lực trong 24 giờ.<br>
                        Nếu bạn không đăng ký tài khoản này, hãy bỏ qua email này.
                    </p>
                </div>
                """.formatted(name, verifyUrl);

        sendHtmlEmail(toEmail, "Xác thực tài khoản PixelMage", html);
    }

    /**
     * Gửi email thông báo khi tài khoản LOCAL được link với Google
     */
    @Async
    public void sendGoogleLinkedNotification(String toEmail, String name) {
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                    <h2 style="color: #4A90E2;">Tài khoản đã được liên kết với Google</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Tài khoản của bạn vừa được liên kết thành công với Google.</p>
                    <p>Từ bây giờ bạn có thể đăng nhập bằng cả email/mật khẩu lẫn Google.</p>
                    <p style="color:#888; font-size:13px;">
                        Nếu bạn không thực hiện hành động này, hãy đổi mật khẩu ngay lập tức.
                    </p>
                </div>
                """.formatted(name);

        sendHtmlEmail(toEmail, "Tài khoản PixelMage đã liên kết Google", html);
    }

    /**
     * Gửi email reset password
     */
    @Async
    public void sendResetPasswordEmail(String toEmail, String name, String token) {
        String resetUrl = frontendUrl + "/auth/reset-password?token=" + token;

        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                    <h2 style="color: #E24A4A;">Đặt lại mật khẩu PixelMage</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>
                    <a href="%s"
                       style="display:inline-block; padding:12px 24px; background:#E24A4A;
                              color:#fff; text-decoration:none; border-radius:6px; margin:16px 0;">
                        Đặt lại mật khẩu
                    </a>
                    <p style="color:#888; font-size:13px;">
                        Link có hiệu lực trong 1 giờ.<br>
                        Nếu bạn không yêu cầu, hãy bỏ qua email này.
                    </p>
                </div>
                """.formatted(name, resetUrl);

        sendHtmlEmail(toEmail, "Đặt lại mật khẩu PixelMage", html);
    }

    // =========================================================
    // TASK-05 — Unlink Request email methods
    // =========================================================

    /**
     * Gửi email xác nhận yêu cầu hủy liên kết thẻ NFC.
     * Token có hiệu lực 10 phút — link verify trỏ về BE endpoint (permit-all).
     */
    @Async
    public void sendUnlinkVerificationEmail(String toEmail, String name, String token) {
        String verifyUrl = frontendUrl + "/api/unlink-requests/verify?token=" + token;

        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                    <h2 style="color: #E2884A;">Xác nhận yêu cầu huỷ liên kết thẻ</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Chúng tôi nhận được yêu cầu huỷ liên kết thẻ NFC của bạn.</p>
                    <p>Nhấn vào nút bên dưới để xác nhận yêu cầu:</p>
                    <a href="%s"
                       style="display:inline-block; padding:12px 24px; background:#E2884A;
                              color:#fff; text-decoration:none; border-radius:6px; margin:16px 0;">
                        Xác nhận yêu cầu huỷ liên kết
                    </a>
                    <p style="color:#888; font-size:13px;">
                        Link có hiệu lực trong 10 phút.<br>
                        Nếu bạn không gửi yêu cầu này, hãy bỏ qua email này.
                    </p>
                </div>
                """.formatted(name, verifyUrl);

        sendHtmlEmail(toEmail, "Xác nhận yêu cầu huỷ liên kết thẻ PixelMage", html);
    }

    /**
     * Gửi email thông báo Staff đã duyệt yêu cầu hủy liên kết.
     */
    @Async
    public void sendUnlinkApprovedEmail(String toEmail, String name, String nfcUid) {
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                    <h2 style="color: #4AE27A;">Yêu cầu huỷ liên kết đã được duyệt</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Yêu cầu huỷ liên kết thẻ <strong>%s</strong> của bạn đã được phê duyệt.</p>
                    <p>Thẻ đã được huỷ liên kết thành công khỏi tài khoản của bạn.</p>
                    <p style="color:#888; font-size:13px;">Cảm ơn bạn đã sử dụng PixelMage.</p>
                </div>
                """.formatted(name, nfcUid);

        sendHtmlEmail(toEmail, "Yêu cầu huỷ liên kết thẻ đã được duyệt — PixelMage", html);
    }

    /**
     * Gửi email thông báo Staff đã từ chối yêu cầu hủy liên kết.
     */
    @Async
    public void sendUnlinkRejectedEmail(String toEmail, String name, String staffNote) {
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
                    <h2 style="color: #E24A4A;">Yêu cầu huỷ liên kết bị từ chối</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Yêu cầu huỷ liên kết thẻ của bạn đã bị từ chối.</p>
                    <p>Lý do: <em>%s</em></p>
                    <p style="color:#888; font-size:13px;">Nếu cần hỗ trợ, hãy liên hệ đội ngũ PixelMage.</p>
                </div>
                """.formatted(name, staffNote != null ? staffNote : "Không có lý do cụ thể");

        sendHtmlEmail(toEmail, "Yêu cầu huỷ liên kết thẻ bị từ chối — PixelMage", html);
    }

    // =========================================================
    // Private helper
    // =========================================================

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, displayName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = html
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            // Log lỗi nhưng không throw — mail thất bại không nên crash flow chính
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
