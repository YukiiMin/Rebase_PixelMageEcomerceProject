package com.example.PixelMageEcomerceProject.security.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.PixelMageEcomerceProject.security.jwt.JwtAuthenticationFilter;
import com.example.PixelMageEcomerceProject.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.example.PixelMageEcomerceProject.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.example.PixelMageEcomerceProject.security.oauth2.OAuth2AuthenticationSuccessHandler;
import com.example.PixelMageEcomerceProject.security.service.CustomUserDetailsService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthFilter;
        private final CustomUserDetailsService customUserDetailsService;
        private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
        private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
        private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;
        private final PasswordEncoder passwordEncoder;

        @Value("${app.frontend.url}")
        private String frontendUrl;

        @Value("${app.dev-frontend.url}")
        private String devFrontendUrl;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(AbstractHttpConfigurer::disable)
                                .formLogin(form -> form.disable())
                                .httpBasic(basic -> basic.disable())
                                .exceptionHandling(exception -> exception
                                                // Xử lý: Nếu chưa xác thực mà đòi vào endpoint private -> Trả về lỗi
                                                // 401 JSON, KHÔNG redirect sang Google
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                                                                        "Unauthorized - Vui lòng cung cấp Token hợp lệ");
                                                }))
                                .authorizeHttpRequests(auth -> auth
                                                // 1. Public hoàn toàn
                                                .requestMatchers(
                                                                "/api/accounts/auth/**",
                                                                "/api/accounts/auth/verify-checkout-token",
                                                                "/error",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/swagger-resources/**",
                                                                "/webjars/**",
                                                                "/ws/**",
                                                                "/api/unlink-requests/verify",
                                                                "/api/payments/webhook/**",
                                                                "/favicon.ico")
                                                .permitAll()
                                                // 2. Public GET endpoints cho Catalog & Marketplace & Card Gallery
                                                .requestMatchers(org.springframework.http.HttpMethod.GET,
                                                                "/api/products",
                                                                "/api/products/**",
                                                                "/api/card-frameworks",
                                                                "/api/card-templates",
                                                                "/api/card-templates/by-framework/**",
                                                                "/api/card-templates/**",
                                                                "/api/card-contents/**",
                                                                "/api/theme-music/active")
                                                .permitAll()
                                                // Protected endpoints - JWT authentication required
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth2 -> oauth2
                                                .authorizationEndpoint(auth -> auth
                                                                .baseUri("/oauth2/authorization")
                                                                .authorizationRequestRepository(
                                                                                cookieAuthorizationRequestRepository))
                                                .redirectionEndpoint(redirection -> redirection
                                                                .baseUri("/login/oauth2/code/*"))
                                                .successHandler(oAuth2AuthenticationSuccessHandler)
                                                .failureHandler(oAuth2AuthenticationFailureHandler))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider())
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                // Allow explicit origins for production and development, and allow patterns for
                // mobile
                configuration.setAllowedOriginPatterns(Arrays.asList(
                                frontendUrl,
                                devFrontendUrl,
                                "http://localhost:3000",
                                "http://localhost:8081",
                                "exp://localhost:8081",
                                "*" // Mở thêm originPattern để không block Mobile (hoặc Vercel deployment preview)
                ));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(customUserDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder);
                return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }
}
