package com.example.PixelMageEcomerceProject.service.interfaces;

import java.util.List;
import java.util.Optional;

import com.example.PixelMageEcomerceProject.dto.request.PackRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Pack;

public interface PackService {
    Pack createPack(PackRequestDTO requestDTO); // This should run RNG to select cards

    Pack updatePackStatus(Integer packId, String status);

    Optional<Pack> getPackById(Integer id);

    List<Pack> getAllPacks();

    List<Pack> getPacksByStatus(String status);

    List<Pack> getPacksByProductAndStatus(Integer productId, String status);

    void deletePack(Integer id);
}
