package io.kk.gameservice.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record GameResponseDTO(
        List<GameDTO> games,
        Integer totalPages
) {
}
