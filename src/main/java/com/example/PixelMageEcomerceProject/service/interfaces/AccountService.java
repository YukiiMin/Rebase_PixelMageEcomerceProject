package com.example.PixelMageEcomerceProject.service.interfaces;

import com.example.PixelMageEcomerceProject.dto.request.RegisterRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.UpdateProfileRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.ChangePasswordRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.ForgotPasswordRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.ResetPasswordRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.LoginRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Account;


import java.util.Map;
import java.util.Optional;

public interface AccountService {

    Account createAccount(RegisterRequestDTO account);

    Account updateAccount(Integer customerId, UpdateProfileRequestDTO account);

    void changePassword(Integer customerId, ChangePasswordRequestDTO dto);

    void forgotPassword(ForgotPasswordRequestDTO dto);

    void resetPassword(ResetPasswordRequestDTO dto);

    void deleteAccount(Integer customerId);

    Optional<Account> getAccountById(Integer customerId);

    Optional<Account> getAccountByEmail(String email);

    org.springframework.data.domain.Page<Account> getAllAccounts(org.springframework.data.domain.Pageable pageable, String roleName);

    Account toggleAccountStatus(Integer customerId);

    boolean existsByEmail(String email);

    Map<String, Object> loginAccount(LoginRequestDTO loginRequestDTO);

    // Email verification
    void verifyEmail(String token);

    void resendVerificationEmail(String email);

    // Token management
    Map<String, Object> refreshAccessToken(String refreshToken);

    void logout(String accessToken, String refreshToken, long tokenRemainingMillis);

    // Mobile OAuth2
    Map<String, Object> verifyGoogleMobileToken(String idTokenString);
}
