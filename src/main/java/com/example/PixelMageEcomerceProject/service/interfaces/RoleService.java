package com.example.PixelMageEcomerceProject.service.interfaces;

import com.example.PixelMageEcomerceProject.dto.request.RoleRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.RoleResponseDTO;

import java.util.List;
import java.util.Optional;

public interface RoleService {

    /**
     * Create a new role
     */
    RoleResponseDTO createRole(RoleRequestDTO roleRequestDTO);

    /**
     * Update an existing role
     */
    RoleResponseDTO updateRole(Integer roleId, RoleRequestDTO roleRequestDTO);

    /**
     * Delete a role by ID
     */
    void deleteRole(Integer roleId);

    /**
     * Get role by ID
     */
    Optional<RoleResponseDTO> getRoleById(Integer roleId);

    /**
     * Get role by name
     */
    Optional<RoleResponseDTO> getRoleByName(String roleName);

    /**
     * Get all roles
     */
    List<RoleResponseDTO> getAllRoles();

    /**
     * Check if role name exists
     */
    boolean existsByRoleName(String roleName);
}
