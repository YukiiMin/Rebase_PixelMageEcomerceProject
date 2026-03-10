package com.example.PixelMageEcomerceProject.service.interfaces;

import java.util.List;
import java.util.Optional;

import com.example.PixelMageEcomerceProject.entity.UserCollectionProgress;

public interface UserCollectionProgressService {
    void recalculateProgressForTemplate(Integer userId, Integer cardTemplateId);

    List<UserCollectionProgress> getUserProgress(Integer userId);

    Optional<UserCollectionProgress> getCollectionProgress(
            Integer userId, Integer collectionId);
}
