package io.kk.gameservice.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ReviewRequestDTO(
        @NotNull
        Long guid,
        @NotBlank
        String gameName,
        String content,
        Integer rating
) {
    @AssertTrue(message = "Review must have either content or a rating (or both)")
    private boolean isAtLeastOneFieldPresent() {
        return (content != null && !content.isBlank()) || rating != null;
    }
}
