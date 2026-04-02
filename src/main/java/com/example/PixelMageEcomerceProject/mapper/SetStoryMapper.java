package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.response.SetStoryResponse;
import com.example.PixelMageEcomerceProject.entity.SetStory;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SetStoryMapper {
    SetStoryResponse toResponse(SetStory setStory);
    List<SetStoryResponse> toResponses(List<SetStory> setStories);
}
