package io.kk.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRegisterDTO(
        @NotBlank
        String username,

        @Email
        @NotBlank
        String email,

        @NotBlank
        String password
) { }