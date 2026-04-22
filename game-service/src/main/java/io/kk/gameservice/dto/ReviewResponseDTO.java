package io.kk.gameservice.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReviewResponseDTO(
        Long id,
        String username,
        String content,
        Integer rating,
        LocalDateTime date
) {
}
