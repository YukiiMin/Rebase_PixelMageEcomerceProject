package com.example.PixelMageEcomerceProject.enums;

/**
 * Payment status enumeration for different payment methods
 */
public enum PaymentStatus {
    PENDING,        // Pending payment
    SUCCEEDED,      // Payment succeeded
    FAILED,         // Payment failed
    CANCELED,       // Payment canceled
    REQUIRES_ACTION // Payment requires action
}
