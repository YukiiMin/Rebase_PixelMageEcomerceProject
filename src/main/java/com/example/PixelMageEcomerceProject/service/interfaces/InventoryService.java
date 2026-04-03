
package com.example.PixelMageEcomerceProject.service.interfaces;

import com.example.PixelMageEcomerceProject.dto.request.InventoryRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface InventoryService {

    /**
     * Create a new inventory record
     */
    Inventory createInventory(InventoryRequestDTO inventoryRequestDTO, int productId);

    /**
     * Update an existing inventory record
     */
    Inventory updateInventory(Integer inventoryId, InventoryRequestDTO inventoryRequestDTO);

    /**
     * Delete an inventory record
     */
    void deleteInventory(Integer inventoryId);

    /**
     * Get an inventory record by ID
     */
    Optional<Inventory> getInventoryById(Integer inventoryId);

    /**
     * Get all inventory records
     */
    List<Inventory> getAllInventories();
    Page<Inventory> getAllInventories(Pageable pageable);

}

