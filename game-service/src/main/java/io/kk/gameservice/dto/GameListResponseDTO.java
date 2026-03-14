package io.kk.gameservice.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record GameListResponseDTO(
        List<GameResponseDTO> wishlist,
        List<GameResponseDTO> gamesToPlay,
        List<GameResponseDTO> completedGames
) { }
