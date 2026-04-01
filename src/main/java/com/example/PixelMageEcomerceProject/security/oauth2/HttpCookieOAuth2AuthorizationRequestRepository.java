package com.example.PixelMageEcomerceProject.security.oauth2;

import java.util.Base64;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Repository to store OAuth2 authorization requests in cookies.
 * Required for Stateless (JWT) architecture where HttpSession is disabled.
 */
@Slf4j
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        log.debug("--- Loading Auth Request ---");
        log.debug("  X-Forwarded-Proto: {}", request.getHeader("X-Forwarded-Proto"));
        log.debug("  X-Forwarded-Host: {}", request.getHeader("X-Forwarded-Host"));
        log.debug("  request.isSecure(): {}", request.isSecure());
        return getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(cookie -> {
                    log.info("FOUND cookie [{}], value starts with: {}",
                            OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                            cookie.getValue().substring(0, Math.min(10, cookie.getValue().length())));
                    return deserialize(cookie, OAuth2AuthorizationRequest.class);
                })
                .orElseGet(() -> {
                    log.warn("MISSING cookie [{}] in request. Total cookies found: {}",
                            OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                            request.getCookies() != null ? request.getCookies().length : 0);
                    return null;
                });
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
            HttpServletResponse response) {
        if (authorizationRequest == null) {
            log.debug("Authorization request is null, deleting cookies");
            deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
            return;
        }

        log.debug("Saving OAuth2 authorization request to cookie");
        addCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serialize(authorizationRequest),
                COOKIE_EXPIRE_SECONDS);
        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (redirectUriAfterLogin != null && !redirectUriAfterLogin.isBlank()) {
            addCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, COOKIE_EXPIRE_SECONDS);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
            HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = this.loadAuthorizationRequest(request);
        if (authRequest != null) {
            removeAuthorizationRequestCookies(request, response);
        }
        return authRequest;
    }

    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }

    public Optional<String> getRedirectUriFromCookie(HttpServletRequest request) {
        return getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);
    }

    // Cookie Helpers

    private Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie cookie = WebUtils.getCookie(request, name);
        return Optional.ofNullable(cookie);
    }


    private void addCookie(HttpServletRequest request, HttpServletResponse response, String name, String value,
            int maxAge) {
        // BẮTBUỘC: SameSite=None; Secure=true cho OAuth2 cross-site redirect.
        // Trình duyệt hiện đại chặn cookie SameSite=Lax khi Google redirect về backend.
        // Luôn force Secure=true vì browser nhìn thấy Ngrok dưới dạng HTTPS.
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .maxAge(maxAge)
                .sameSite("None")
                .secure(true)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        log.info("Added cookie [{}], value length: {}, SameSite: None, Secure: true", name, value.length());
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        // Phải khớp flag với lúc tạo: SameSite=None; Secure=true
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .sameSite("None")
                .secure(true)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        log.info("Deleted cookie [{}]", name);
    }

    private String serialize(Object object) {
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(object));
    }

    @SuppressWarnings("deprecation")
    private <T> T deserialize(Cookie cookie, Class<T> cls) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cookie.getValue());
            return cls.cast(SerializationUtils.deserialize(decoded));
        } catch (Exception e) {
            log.error("FAILED to deserialize OAuth2 authorization request from cookie '{}'", cookie.getName(), e);
            return null;
        }
    }
}
