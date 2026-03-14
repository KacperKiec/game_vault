package io.kk.gameservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record GameRequestDTO(
        @NotBlank
        String name,

        @NotBlank
        String category,

        @NotBlank
        String squareAvatarImageURL,

        @NotBlank
        String squareMediumImageURL,

        @NotNull
        LocalDate releaseDate,

        @NotBlank
        String description
) { }
