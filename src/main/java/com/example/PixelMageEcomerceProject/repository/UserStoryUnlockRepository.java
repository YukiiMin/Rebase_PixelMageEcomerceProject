package com.example.PixelMageEcomerceProject.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.PixelMageEcomerceProject.entity.UserStoryUnlock;

@Repository
public interface UserStoryUnlockRepository extends JpaRepository<UserStoryUnlock, Integer> {

    boolean existsByUser_CustomerIdAndStory_StoryId(Integer userId, Integer storyId);

    List<UserStoryUnlock> findByUser_CustomerId(Integer userId);
}

