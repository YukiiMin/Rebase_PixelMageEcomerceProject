package com.example.PixelMageEcomerceProject.service.interfaces;

import com.example.PixelMageEcomerceProject.dto.request.PackCategoryRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.PackCategoryResponse;

import java.util.List;

public interface PackCategoryService {
    PackCategoryResponse createPackCategory(PackCategoryRequestDTO requestDTO);
    PackCategoryResponse updatePackCategory(Integer id, PackCategoryRequestDTO requestDTO);
    void deletePackCategory(Integer id);
    PackCategoryResponse getPackCategoryById(Integer id);
    List<PackCategoryResponse> getAllPackCategories();
    PackCategoryResponse toggleActive(Integer id);
}
