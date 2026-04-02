package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.response.SpreadResponse;
import com.example.PixelMageEcomerceProject.entity.Spread;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SpreadMapper {
    SpreadResponse toResponse(Spread spread);
    List<SpreadResponse> toResponses(List<Spread> spreads);
}
