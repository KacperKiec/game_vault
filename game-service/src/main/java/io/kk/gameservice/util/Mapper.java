package io.kk.gameservice.util;

import io.kk.gameservice.dto.GameListResponseDTO;
import io.kk.gameservice.dto.GameListedResponseDTO;
import io.kk.gameservice.dto.GameResponseDTO;
import io.kk.gameservice.model.ListType;

import java.util.List;

public class Mapper {
    public static GameResponseDTO toGameDTO(Game game, boolean avatar) {
        GameResponseDTO dto = new GameResponseDTO();
        dto.setGuid(game.guid());
        dto.setName(game.name());
        dto.setCategory(game.genres());
        dto.setImageURL(game.backgroundImg());
        dto.setReleaseDate(game.releaseDate());
        dto.setDescription(game.description());
        return dto;
    }

    public static GameListedResponseDTO toGameListedDTO(Game game, boolean avatar, ListType listType) {
        GameListedResponseDTO dto = new GameListedResponseDTO();
        dto.setGuid(game.guid());
        dto.setName(game.name());
        dto.setCategory(game.genres());
        dto.setImageURL(game.backgroundImg());
        dto.setReleaseDate(game.releaseDate());
        dto.setDescription(game.description());
        dto.setListType(listType);
        return dto;
    }

    public static GameListResponseDTO toGameListResponseDTO(List<Game> wishlist, List<Game> gamesToPlay, List<Game> completedGames) {
        return GameListResponseDTO.builder()
                .wishlist(wishlist.stream().map(g -> Mapper.toGameDTO(g, true)).toList())
                .gamesToPlay(gamesToPlay.stream().map(g -> Mapper.toGameDTO(g, true)).toList())
                .completedGames(completedGames.stream().map(g -> Mapper.toGameDTO(g, true)).toList())
                .build();
    }
}
