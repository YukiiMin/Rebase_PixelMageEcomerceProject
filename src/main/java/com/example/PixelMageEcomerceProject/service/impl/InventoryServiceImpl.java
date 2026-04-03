package com.example.PixelMageEcomerceProject.service.impl;

import com.example.PixelMageEcomerceProject.dto.request.InventoryRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Inventory;
import com.example.PixelMageEcomerceProject.entity.Product;
import com.example.PixelMageEcomerceProject.repository.InventoryRepository;
import com.example.PixelMageEcomerceProject.repository.ProductRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Inventory createInventory(InventoryRequestDTO inventoryRequestDTO, int productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // Check if inventory already exists for this product
        Optional<Inventory> existingInventory = inventoryRepository.findByProductId(productId);

        if (existingInventory.isPresent()) {
            throw new RuntimeException("Inventory already exists for product " + productId);
        }

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(inventoryRequestDTO.getQuantity());
        inventory.setLastChecked(inventoryRequestDTO.getLastChecked());

        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public Inventory updateInventory(Integer inventoryId, InventoryRequestDTO inventoryRequestDTO) {
        Inventory existingInventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory not found with id: " + inventoryId));

//        existingInventory.setProductId(inventoryRequestDTO.getProductId());
        existingInventory.setQuantity(inventoryRequestDTO.getQuantity());
        existingInventory.setLastChecked(inventoryRequestDTO.getLastChecked());

        return inventoryRepository.save(existingInventory);
    }

    @Override
    @Transactional
    public void deleteInventory(Integer inventoryId) {
        if (!inventoryRepository.existsById(inventoryId)) {
            throw new RuntimeException("Inventory not found with id: " + inventoryId);
        }
        inventoryRepository.deleteById(inventoryId);
    }

    @Override
    public Optional<Inventory> getInventoryById(Integer inventoryId) {
        return inventoryRepository.findById(inventoryId);
    }

    @Override
    public List<Inventory> getAllInventories() {
        return inventoryRepository.findAll();
    }

    @Override
    public Page<Inventory> getAllInventories(Pageable pageable) {
        return inventoryRepository.findAll(pageable);
    }

}

