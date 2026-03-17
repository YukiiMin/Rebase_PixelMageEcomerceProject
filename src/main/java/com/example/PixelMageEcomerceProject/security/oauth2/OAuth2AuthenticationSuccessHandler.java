package com.example.PixelMageEcomerceProject.security.oauth2;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.Role;
import com.example.PixelMageEcomerceProject.enums.AuthProvider;
import com.example.PixelMageEcomerceProject.repository.AccountRepository;
import com.example.PixelMageEcomerceProject.repository.RoleRepository;
import com.example.PixelMageEcomerceProject.security.jwt.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OAuth2 Authentication Success Handler
 * Handles successful Google OAuth2 authentication and integrates with existing
 * JWT system
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        try {
            // Extract user information from Google
            Map<String, Object> attributes = oAuth2User.getAttributes();
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            String googleId = (String) attributes.get("sub");

            log.info("Processing OAuth2 authentication for user: {}", email);

            // Find or create account using repositories directly
            Account account = processOAuth2Account(email, name, googleId);

            // Generate JWT token using JwtTokenProvider
            String jwtToken = jwtTokenProvider.generateToken(
                    org.springframework.security.core.userdetails.User.builder()
                            .username(account.getEmail())
                            .password("") // OAuth2 accounts don't need passwords for JWT
                            .authorities(account.getAuthorities())
                            .build());

            // Redirect to frontend with token
            String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/success")
                    .queryParam("token", jwtToken)
                    .queryParam("email", account.getEmail())
                    .queryParam("name", account.getName())
                    .build()
                    .toUriString();

            log.info("Redirecting user {} to: {}", email, redirectUrl);
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        } catch (Exception e) {
            log.error("Error processing OAuth2 authentication", e);

            // Redirect to error page
            String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/error")
                    .queryParam("error", "authentication_failed")
                    .build()
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }

    /**
     * Process OAuth2 account - find existing account or create new one
     */
    private Account processOAuth2Account(String email, String name, String googleId) {
        Optional<Account> existingAccount = accountRepository.findByEmail(email);

        if (existingAccount.isPresent()) {
            return linkOAuth2Provider(existingAccount.get(), googleId);
        } else {
            return createOAuth2Account(email, name, googleId);
        }
    }

    /**
     * Link OAuth2 provider to existing account
     */
    private Account linkOAuth2Provider(Account existingAccount, String providerId) {
        if (existingAccount.getAuthProvider() == AuthProvider.LOCAL) {
            // Link OAuth2 to local account
            existingAccount.setAuthProvider(AuthProvider.GOOGLE);
            existingAccount.setProviderId(providerId);
            existingAccount.setUpdatedAt(LocalDateTime.now());
            log.info("Linked Google OAuth2 to existing local account: {}", existingAccount.getEmail());
        } else if (!providerId.equals(existingAccount.getProviderId())) {
            // Update provider ID if changed
            existingAccount.setProviderId(providerId);
            existingAccount.setUpdatedAt(LocalDateTime.now());
        }

        return accountRepository.save(existingAccount);
    }

    /**
     * Create new OAuth2 account with customer role
     */
    private Account createOAuth2Account(String email, String name, String providerId) {
        // Find customer role
        Role customerRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new RuntimeException("USER role not found. Please ensure roles are initialized."));

        Account newAccount = new Account();
        newAccount.setEmail(email);
        newAccount.setName(name);
        newAccount.setAuthProvider(AuthProvider.GOOGLE);
        newAccount.setProviderId(providerId);
        newAccount.setRole(customerRole);
        // Password is null for OAuth2 accounts
        newAccount.setCreatedAt(LocalDateTime.now());
        newAccount.setUpdatedAt(LocalDateTime.now());

        log.info("Creating new Google OAuth2 account for: {}", email);
        return accountRepository.save(newAccount);
    }
}
