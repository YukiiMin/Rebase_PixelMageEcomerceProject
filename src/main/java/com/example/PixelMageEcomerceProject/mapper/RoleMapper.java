package com.example.PixelMageEcomerceProject.mapper;

import com.example.PixelMageEcomerceProject.dto.request.RoleRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.RoleResponseDTO;
import com.example.PixelMageEcomerceProject.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {
    RoleResponseDTO toResponse(Role role);
    Role toEntity(RoleRequestDTO roleRequestDTO);
    List<RoleResponseDTO> toResponses(List<Role> roles);
}
