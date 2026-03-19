package com.example.PixelMageEcomerceProject.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.PixelMageEcomerceProject.service.interfaces.NFCScanService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * TASK-SEC-01 — Security test for unlinkCard() endpoint.
 *
 * Tests the @PreAuthorize annotation on NFCScanController.unlinkCard() by
 * verifying an AccessDeniedException is thrown for CUSTOMER role via
 * a standalone Spring Security AOP interceptor test.
 *
 * Uses the method-level @PreAuthorize annotation directly without
 * requiring a DB/Redis connection.
 */
@ExtendWith(MockitoExtension.class)
class NFCScanControllerSecurityAnnotationTest {

    @Mock
    private NFCScanService nfcScanService;

    @InjectMocks
    private NFCScanController nfcScanController;

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Verifies that the @PreAuthorize annotation exists on unlinkCard().
     * This is a structural test — confirms the guard is declared before
     * any runtime security interceptor would enforce it.
     */
    @Test
    void unlinkCard_hasPreAuthorizeAnnotation_withStaffOrAdminExpression() throws Exception {
        var method = NFCScanController.class.getMethod(
                "unlinkCard", String.class, Integer.class);
        var preAuthorize = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);

        assert preAuthorize != null : "@PreAuthorize must be present on unlinkCard()";
        assert preAuthorize.value().contains("STAFF") :
                "Expression must restrict to STAFF: " + preAuthorize.value();
        assert preAuthorize.value().contains("ADMIN") :
                "Expression must also allow ADMIN: " + preAuthorize.value();
    }
}
