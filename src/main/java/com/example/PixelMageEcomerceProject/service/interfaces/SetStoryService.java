package com.example.PixelMageEcomerceProject.service.interfaces;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.PixelMageEcomerceProject.entity.SetStory;

@Service
public interface SetStoryService {

    void checkAndUnlockStories(Integer userId);

    void revokeStoriesIfConditionNotMet(Integer userId);

    List<SetStory> getAllStories();

    List<SetStory> getUnlockedStories(Integer userId);

    SetStory createStory(SetStory story);

    SetStory getStoryById(Integer id);

    SetStory updateStory(SetStory story);

    void deleteStory(Integer id);
}

