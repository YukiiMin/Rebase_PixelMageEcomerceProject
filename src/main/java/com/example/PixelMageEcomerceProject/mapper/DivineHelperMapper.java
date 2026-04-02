package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.response.DivineHelperResponse;
import com.example.PixelMageEcomerceProject.entity.DivineHelper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DivineHelperMapper {
    DivineHelperResponse toResponse(DivineHelper divineHelper);
    List<DivineHelperResponse> toResponses(List<DivineHelper> divineHelpers);
}
