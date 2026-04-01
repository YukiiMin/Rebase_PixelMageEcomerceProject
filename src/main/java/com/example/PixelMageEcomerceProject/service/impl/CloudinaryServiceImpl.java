package com.example.PixelMageEcomerceProject.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.PixelMageEcomerceProject.service.interfaces.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !originalFilename.matches(".*\\.(jpg|png|jpeg|webp)$")) {
            throw new IllegalArgumentException("Unsupported file type. Allowed: jpg, png, jpeg, webp");
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
            "folder", folder,
            "public_id", UUID.randomUUID().toString()
        ));

        return uploadResult.get("secure_url").toString();
    }

    @Override
    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
