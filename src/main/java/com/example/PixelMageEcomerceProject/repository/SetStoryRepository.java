package com.example.PixelMageEcomerceProject.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.PixelMageEcomerceProject.entity.SetStory;

@Repository
public interface SetStoryRepository extends JpaRepository<SetStory, Integer> {

    List<SetStory> findByIsActiveTrue();
}

