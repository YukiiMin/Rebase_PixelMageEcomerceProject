package com.example.PixelMageEcomerceProject.service.interfaces;

import java.util.List;
import java.util.Optional;

import com.example.PixelMageEcomerceProject.dto.request.PackRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.PackResponse;
import com.example.PixelMageEcomerceProject.enums.PackStatus;

public interface PackService {
    PackResponse createPack(PackRequestDTO requestDTO); // This should run RNG to select cards

    Optional<PackResponse> getPackById(Integer id);

    List<PackResponse> getAllPacks();

    List<PackResponse> getPacksByStatus(PackStatus status);

    List<PackResponse> getPacksByProductAndStatus(Integer productId, PackStatus status);

    void deletePack(Integer id);

    PackResponse updatePackStatus(Integer packId, PackStatus status);
}
