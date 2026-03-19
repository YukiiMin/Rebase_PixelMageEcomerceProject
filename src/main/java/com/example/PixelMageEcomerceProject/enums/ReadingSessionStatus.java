package com.example.PixelMageEcomerceProject.enums;

/**
 * Reading session status enumeration for different reading session methods
 */
public enum ReadingSessionStatus {
    PENDING,      // Chờ xử lý
    INTERPRETING, // Đang xử lý
    COMPLETED,    // Đã xử lý
    EXPIRED       // EXPLORE session TTL hết hạn (30 phút)
}
