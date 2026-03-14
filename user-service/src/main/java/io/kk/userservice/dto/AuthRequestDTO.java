package io.kk.userservice.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequestDTO(
        @NotBlank
        String username,

        @NotBlank
        String password
) {
}
