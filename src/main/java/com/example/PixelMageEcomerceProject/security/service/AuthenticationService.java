package com.example.PixelMageEcomerceProject.security.service;

import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    // private final AuthenticationManager authenticationManager;
    private final AccountRepository accountRepository;

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public String generateToken(Account account) {
        return jwtTokenProvider.generateToken(
            org.springframework.security.core.userdetails.User.builder()
                .username(account.getEmail())
                .password(account.getPassword() != null ? account.getPassword() : "")
                .authorities(account.getAuthorities())
                .build()
        );
    }

    /**
     * Check if account exists by email
     */
    public boolean accountExistsByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }

    /**
     * Validate OAuth2 account creation
     * Ensures the account can be created with the given email
     */
    public boolean canCreateOAuth2Account(String email) {
        return accountRepository.findByEmail(email)
            .map(existing -> existing.getAuthProvider() == com.example.PixelMageEcomerceProject.entity.AuthProvider.LOCAL)
            .orElse(true); // Can create if no account exists
    }
}
