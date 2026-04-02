package com.example.PixelMageEcomerceProject.enums;

public enum ContentType {
    STORY,    // Short story, lore, narrative — stored as raw text in contentData
    IMAGE,   // Cloudinary image URL
    VIDEO,   // Cloudinary video URL (short clip)
    GIF,     // Cloudinary animated GIF URL
    LINK     // External reference link
}
