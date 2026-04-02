package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.response.UserCollectionProgressResponse;
import com.example.PixelMageEcomerceProject.entity.UserCollectionProgress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserCollectionProgressMapper {

    @Mapping(target = "collectionId", source = "collection.collectionId")
    @Mapping(target = "collectionName", source = "collection.collectionName")
    @Mapping(target = "collectionType", source = "collection.collectionType")
    @Mapping(target = "isPublic", source = "collection.isPublic")
    @Mapping(target = "rewardType", source = "collection.rewardType")
    UserCollectionProgressResponse toResponse(UserCollectionProgress progress);

    List<UserCollectionProgressResponse> toResponses(List<UserCollectionProgress> progressList);
}
