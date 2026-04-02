package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.response.UnlinkRequestResponse;
import com.example.PixelMageEcomerceProject.entity.UnlinkRequest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UnlinkRequestMapper {
    UnlinkRequestResponse toResponse(UnlinkRequest unlinkRequest);
    List<UnlinkRequestResponse> toResponses(List<UnlinkRequest> unlinkRequests);
}
