package com.example.PixelMageEcomerceProject.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.PixelMageEcomerceProject.dto.ThemeMusicDto;

public interface ThemeMusicService {

    /** Upload file âm nhạc lên Cloudinary, lưu vào DB */
    ThemeMusicDto.Response upload(MultipartFile file, String title, String artist);

    /** Lấy tất cả bài nhạc */
    List<ThemeMusicDto.Response> findAll();

    /** Lấy bài đang active (public endpoint) */
    ThemeMusicDto.Response findActive();

    /** Admin kích hoạt một bài (deactivate tất cả trước) */
    ThemeMusicDto.Response activate(Long musicId);

    /** Admin xoá bài khỏi DB + Cloudinary */
    void delete(Long musicId);
}
