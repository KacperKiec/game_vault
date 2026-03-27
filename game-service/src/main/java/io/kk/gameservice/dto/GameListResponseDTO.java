package io.kk.gameservice.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record GameListResponseDTO(
        List<GameDetailsDTO> wishlist,
        List<GameDetailsDTO> gamesToPlay,
        List<GameDetailsDTO> completedGames
) { }
