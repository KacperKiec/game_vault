package io.kk.userservice.dto;

import io.kk.userservice.model.Role;
import lombok.Builder;

@Builder
public record UserResponseDTO(
        Long id,
        String username,
        String email,
        Role role
) { }