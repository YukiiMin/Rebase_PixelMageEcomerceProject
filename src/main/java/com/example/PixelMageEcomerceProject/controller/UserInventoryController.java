package com.example.PixelMageEcomerceProject.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.entity.UserInventory;
import com.example.PixelMageEcomerceProject.service.interfaces.UserInventoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class UserInventoryController {

    private final UserInventoryService userInventoryService;

    @GetMapping("/my-cards")
    public ResponseEntity<ResponseBase<List<UserInventory>>> getMyInventory(@RequestParam("userId") Integer userId) {
        List<UserInventory> inventoryList = userInventoryService.getUserInventory(userId);
        return ResponseBase.ok(inventoryList, "Success");
    }

    @GetMapping("/my-cards/{templateId}")
    public ResponseEntity<ResponseBase<UserInventory>> getMyInventoryByTemplate(
            @RequestParam("userId") Integer userId,
            @PathVariable Integer templateId) {
        List<UserInventory> inventoryList = userInventoryService.getUserInventory(userId);
        UserInventory result = inventoryList.stream()
                .filter(inv -> inv.getCardTemplate().getCardTemplateId().equals(templateId))
                .findFirst()
                .orElse(null);

        if (result == null) {
            return ResponseBase.error(HttpStatus.NOT_FOUND, "Template not found in user inventory");
        }
        return ResponseBase.ok(result, "Success");
    }
}
