package com.example.PixelMageEcomerceProject.enums;

public enum CardProductStatus {
    PENDING_BIND, // Tạo trong DB, chưa gắn NFC
    READY, // Đã binding NFC, sẵn sàng bán
    SOLD, // Đã đóng gói trong order
    LINKED, // Customer đã scan NFC thành công
    DEACTIVATED // Admin vô hiệu hóa
}
