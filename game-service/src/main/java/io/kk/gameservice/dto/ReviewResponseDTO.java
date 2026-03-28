package io.kk.gameservice.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ReviewResponseDTO(
        Long id,
        String username,
        String content,
        Integer rating,
        LocalDate date
) {
}
