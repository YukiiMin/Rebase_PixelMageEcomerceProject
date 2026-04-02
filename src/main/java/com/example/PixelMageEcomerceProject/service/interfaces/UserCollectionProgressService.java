package com.example.PixelMageEcomerceProject.service.interfaces;

import com.example.PixelMageEcomerceProject.dto.response.UserCollectionProgressResponse;

import java.util.List;
import java.util.Optional;

public interface UserCollectionProgressService {
    void recalculateProgressForTemplate(Integer userId, Integer cardTemplateId);

    List<UserCollectionProgressResponse> getUserProgress(Integer userId);

    Optional<UserCollectionProgressResponse> getCollectionProgress(
            Integer userId, Integer collectionId);
}
