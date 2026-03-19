package com.example.PixelMageEcomerceProject.service.interfaces;

import java.util.List;

import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.entity.UserInventory;

public interface UserInventoryService {
    UserInventory upsertInventory(Integer userId, Integer cardTemplateId, int quantityChange);

    List<UserInventory> getUserInventory(Integer userId);

    List<CardTemplate> getLinkedCardTemplates(Integer userId);

    int getLinkedCardCount(Integer userId);
}
