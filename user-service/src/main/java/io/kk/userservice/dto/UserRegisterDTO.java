package io.kk.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRegisterDTO(
        @NotBlank
        String username,

        @Email(regexp=".+@.+\\..+", message="Please provide a valid email address")
        @NotBlank
        String email,

        @NotBlank
        String password
) { }