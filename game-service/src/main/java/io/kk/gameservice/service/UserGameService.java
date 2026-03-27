package io.kk.gameservice.service;

import io.kk.gameservice.dto.GameDetailsDTO;
import io.kk.gameservice.dto.GameListRequestDTO;
import io.kk.gameservice.dto.GameListResponseDTO;
import io.kk.gameservice.exception.GameNotFoundException;
import io.kk.gameservice.exception.UserNotFoundException;
import io.kk.gameservice.model.GameList;
import io.kk.gameservice.model.ListType;
import io.kk.gameservice.repository.GameListRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserGameService {

    private final GameListRepository gameListRepository;
    private final GameAPIService gameAPIService;
    private final InternalServiceClient internalServiceClient;

    @Transactional
    public void modifyLists(GameListRequestDTO gameListRequestDTO, Long userId) {
        var userExists = internalServiceClient.checkIfUserExists(userId);

        var game = gameAPIService.getGame(gameListRequestDTO.guid());

        if (!userExists) throw new UserNotFoundException("User not found");

        if (game == null) throw new GameNotFoundException("Game not found");

        var gameList = gameListRepository.findByUserIdAndGameId(userId, game.guid());

        if (gameList.isEmpty()) {
            var gl = new GameList();
            gl.setUserId(userId);
            gl.setGameId(game.guid());
            gl.setListType(gameListRequestDTO.listType());

            gameListRepository.save(gl);

        } else if (gameListRequestDTO.listType() == ListType.NONE) {
            gameListRepository.delete(gameList.get());
        } else {
            gameList.get().setListType(gameListRequestDTO.listType());
            gameListRepository.save(gameList.get());
        }
    }

    List<GameDetailsDTO> gameListCreator(Long userId, ListType listType) {
        return gameListRepository.findByUserIdAndListType(userId, listType).stream()
                .map((gameList) -> gameAPIService.getGame(gameList.getGameId()))
                .toList();
    }

    public GameListResponseDTO getLists(Long userId) {
        var userExists = internalServiceClient.checkIfUserExists(userId);

        if (!userExists) throw new UserNotFoundException("User not found");

        return GameListResponseDTO.builder()
                .wishlist(gameListCreator(userId, ListType.WISHLIST))
                .gamesToPlay(gameListCreator(userId, ListType.TODO))
                .completedGames(gameListCreator(userId, ListType.COMPLETED))
                .build();
    }
}