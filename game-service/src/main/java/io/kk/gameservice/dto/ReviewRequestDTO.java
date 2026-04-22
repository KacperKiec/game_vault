package io.kk.gameservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ReviewRequestDTO(
        @NotNull
        Long guid,
        @NotBlank
        String gameName,
        @NotBlank
        String content,
        @NotNull
        Integer rating
) {
}
