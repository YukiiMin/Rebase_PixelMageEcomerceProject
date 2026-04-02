package com.example.PixelMageEcomerceProject.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.PixelMageEcomerceProject.dto.request.RoleRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.dto.response.RoleResponseDTO;
import com.example.PixelMageEcomerceProject.service.interfaces.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "APIs for managing roles")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

        private final RoleService roleService;

        /**
         * Create a new role
         */
        @PostMapping
        @Operation(summary = "Create a new role", description = "Create a new role with role name")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Role created successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request - Role name already exists", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<RoleResponseDTO>> createRole(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Role details to create", required = true, content = @Content(schema = @Schema(implementation = RoleRequestDTO.class))) @RequestBody RoleRequestDTO roleRequestDTO) {
                try {
                        RoleResponseDTO createdRole = roleService.createRole(roleRequestDTO);
                        return ResponseBase.created(createdRole, "Role created successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST, "Failed to create role: " + e.getMessage());
                }
        }

        /**
         * Get all roles
         */
        @GetMapping
        @Operation(summary = "Get all roles", description = "Retrieve a list of all roles in the system")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Roles retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<RoleResponseDTO>>> getAllRoles() {
                List<RoleResponseDTO> roles = roleService.getAllRoles();
                return ResponseBase.ok(roles, "Roles retrieved successfully");
        }

        /**
         * Get role by ID
         */
        @GetMapping("/{id}")
        @Operation(summary = "Get role by ID", description = "Retrieve role details by role ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Role retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Role not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<RoleResponseDTO>> getRoleById(
                        @Parameter(description = "Role ID", required = true) @PathVariable Integer id) {
                return roleService.getRoleById(id)
                                .map(role -> ResponseBase.ok(role, "Role retrieved successfully"))
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Role not found with ID: " + id));
        }

        /**
         * Get role by name
         */
        @GetMapping("/name/{roleName}")
        @Operation(summary = "Get role by name", description = "Retrieve role details by role name")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Role retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Role not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<RoleResponseDTO>> getRoleByName(
                        @Parameter(description = "Role name", required = true) @PathVariable String roleName) {
                return roleService.getRoleByName(roleName)
                                .map(role -> ResponseBase.ok(role, "Role retrieved successfully"))
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Role not found with name: " + roleName));
        }

        /**
         * Update role
         */
        @PutMapping("/{id}")
        @Operation(summary = "Update role", description = "Update existing role information")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Role updated successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request - Invalid data or role name already exists", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Role not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<RoleResponseDTO>> updateRole(
                        @Parameter(description = "Role ID", required = true) @PathVariable Integer id,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated role details", required = true, content = @Content(schema = @Schema(implementation = RoleRequestDTO.class))) @RequestBody RoleRequestDTO roleRequestDTO) {
                try {
                        RoleResponseDTO updatedRole = roleService.updateRole(id, roleRequestDTO);
                        return ResponseBase.ok(updatedRole, "Role updated successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST, "Failed to update role: " + e.getMessage());
                }
        }

        /**
         * Delete role
         */
        @DeleteMapping("/{id}")
        @Operation(summary = "Delete role", description = "Delete a role by role ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Role deleted successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Role not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Void>> deleteRole(
                        @Parameter(description = "Role ID", required = true) @PathVariable Integer id) {
                try {
                        roleService.deleteRole(id);
                        return ResponseBase.success("Role deleted successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, "Failed to delete role: " + e.getMessage());
                }
        }

        /**
         * Check if role name exists
         */
        @GetMapping("/exists/{roleName}")
        @Operation(summary = "Check if role name exists", description = "Check if a role name is already registered in the system")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Role name check completed", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Boolean>> checkRoleNameExists(
                        @Parameter(description = "Role name to check", required = true) @PathVariable String roleName) {
                boolean exists = roleService.existsByRoleName(roleName);
                return ResponseBase.ok(exists, "Role name check completed");
        }
}
