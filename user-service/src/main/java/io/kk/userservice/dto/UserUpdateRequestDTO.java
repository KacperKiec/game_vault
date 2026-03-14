package io.kk.userservice.dto;

import io.kk.userservice.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserUpdateRequestDTO(
        @NotNull
        Long id,

        @NotBlank
        String username,

        @Email
        @NotBlank
        String email,

        @NotNull
        Role role
) {
}
