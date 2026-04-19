package com.example.PixelMageEcomerceProject.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.PixelMageEcomerceProject.dto.response.PackCategoryResponse;
import com.example.PixelMageEcomerceProject.entity.PackCategory;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { CardTemplateMapper.class })
public interface PackCategoryMapper {

    PackCategoryResponse toResponse(PackCategory packCategory);
}
