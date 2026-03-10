package com.example.PixelMageEcomerceProject.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.ApiException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.RateLimitException;
import com.stripe.exception.StripeException;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for payment-related and general application
 * exceptions.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ResponseBase> handlePaymentNotFoundException(PaymentNotFoundException ex,
            WebRequest request) {
        log.error("Payment not found: {}", ex.getMessage());
        ResponseBase response = new ResponseBase(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ResponseBase> handlePaymentProcessingException(PaymentProcessingException ex,
            WebRequest request) {
        log.error("Payment processing error: {}", ex.getMessage(), ex);
        ResponseBase response = new ResponseBase(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InvalidPaymentStateException.class)
    public ResponseEntity<ResponseBase> handleInvalidPaymentStateException(InvalidPaymentStateException ex,
            WebRequest request) {
        log.error("Invalid payment state: {}", ex.getMessage());
        ResponseBase response = new ResponseBase(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // Stripe-specific exception handlers
    @ExceptionHandler(CardException.class)
    public ResponseEntity<ResponseBase> handleCardException(CardException ex, WebRequest request) {
        log.error("Stripe card error: {}", ex.getMessage());
        ResponseBase response = new ResponseBase(
                HttpStatus.BAD_REQUEST.value(),
                "Card error: " + ex.getUserMessage(),
                null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ResponseBase> handleRateLimitException(RateLimitException ex, WebRequest request) {
        log.error("Stripe rate limit exceeded: {}", ex.getMessage());
        ResponseBase response = new ResponseBase(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Rate limit exceeded. Please try again later.",
                null);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ResponseBase> handleInvalidRequestException(InvalidRequestException ex, WebRequest request) {
        log.error("Stripe invalid request: {}", ex.getMessage());
        ResponseBase response = new ResponseBase(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid request: " + ex.getUserMessage(),
                null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseBase> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.error("Stripe authentication error: {}", ex.getMessage());
        ResponseBase response = new ResponseBase(
                HttpStatus.UNAUTHORIZED.value(),
                "Payment service authentication failed",
                null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ApiConnectionException.class)
    public ResponseEntity<ResponseBase> handleApiConnectionException(ApiConnectionException ex, WebRequest request) {
        log.error("Stripe API connection error: {}", ex.getMessage());
        ResponseBase response = new ResponseBase(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Payment service temporarily unavailable. Please try again later.",
                null);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ResponseBase> handleApiException(ApiException ex, WebRequest request) {
        log.error("Stripe API error: {}", ex.getMessage());
        ResponseBase response = new ResponseBase(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Payment service error. Please try again later.",
                null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(StripeException.class)
    public ResponseEntity<ResponseBase> handleStripeException(StripeException ex, WebRequest request) {
        log.error("General Stripe error: {}", ex.getMessage());
        ResponseBase response = new ResponseBase(
                HttpStatus.BAD_REQUEST.value(),
                "Payment processing failed: " + ex.getUserMessage(),
                null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseBase> handleIllegalArgumentException(IllegalArgumentException ex,
            WebRequest request) {
        log.error("Invalid argument: {}", ex.getMessage());
        ResponseBase response = new ResponseBase(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid request parameters: " + ex.getMessage(),
                null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseBase> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("Runtime error: {}", ex.getMessage(), ex);
        ResponseBase response = new ResponseBase(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred: " + ex.getMessage(),
                null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseBase> handleGeneralException(Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        ResponseBase response = new ResponseBase(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred: " + ex.getMessage(),
                null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
