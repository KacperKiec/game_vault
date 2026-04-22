package io.kk.gameservice.service;

import io.kk.envelope.IntegrationEvent;
import io.kk.gameservice.dto.GameDetailsDTO;
import io.kk.gameservice.dto.GameListRequestDTO;
import io.kk.gameservice.dto.GameListResponseDTO;
import io.kk.gameservice.exception.GameNotFoundException;
import io.kk.gameservice.exception.UserNotFoundException;
import io.kk.gameservice.integration.InternalServiceClient;
import io.kk.gameservice.integration.RabbitService;
import io.kk.gameservice.model.GameList;
import io.kk.gameservice.model.ListType;
import io.kk.gameservice.repository.GameListRepository;
import io.kk.payload.GameMovedBetweenListsPayload;
import io.kk.payload.GameToListPayload;
import io.kk.type.EventType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsible for managing user-specific game collections.
 * It handles adding games to different list types (Wishlist, To Play, Completed),
 * moving games between lists, and retrieving a user's entire collection.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserGameService {

    private final GameListRepository gameListRepository;
    private final GameAPIService gameAPIService;
    private final InternalServiceClient internalServiceClient;
    private final RabbitService rabbitService;

    /**
     * Modifies the state of a game within a user's collection.
     * Depending on the provided {@link ListType}, it creates a new entry,
     * updates an existing one, or removes the game from all lists if the type is NONE.
     *
     * @param gameListRequestDTO DTO containing the game identifier and the target list type.
     * @param userId             The unique identifier of the user.
     * @throws UserNotFoundException if the provided user ID does not exist in the system.
     * @throws GameNotFoundException if the game cannot be found in the external API.
     */
    @Transactional
    public void modifyLists(GameListRequestDTO gameListRequestDTO, Long userId) {
        var userExists = internalServiceClient.checkIfUserExists(userId);

        var game = gameAPIService.getGame(gameListRequestDTO.guid(), userId);

        if (!userExists) throw new UserNotFoundException("User not found");

        if (game == null) throw new GameNotFoundException("Game not found");

        var gameList = gameListRepository.findByUserIdAndGameId(userId, game.guid());

        if (gameList.isEmpty()) {
            var gl = new GameList();
            gl.setUserId(userId);
            gl.setGameId(game.guid());
            gl.setListType(gameListRequestDTO.listType());

            gameListRepository.save(gl);
            sendDashboardEvent(userId, game, EventType.GAME_ADDED_TO_LIST, ListType.NONE);

        } else if (gameListRequestDTO.listType() == ListType.NONE) {
            gameListRepository.delete(gameList.get());
            sendDashboardEvent(userId, game, EventType.GAME_REMOVED_FROM_LIST, gameList.get().getListType());
        } else {
            var oldListType = gameList.get().getListType();
            gameList.get().setListType(gameListRequestDTO.listType());
            gameListRepository.save(gameList.get());
            sendDashboardEvent(userId, game, EventType.GAME_MOVED_BETWEEN_LISTS, oldListType);
        }
    }

    /**
     * Internal helper method to fetch and map games for a specific list type.
     * It retrieves game IDs from the local database and fetches full details from the external API.
     *
     * @param userId   The ID of the user.
     * @param listType The specific category of games to retrieve.
     * @return A list of {@link GameDetailsDTO} objects for the specified list.
     */
    List<GameDetailsDTO> gameListCreator(Long userId, ListType listType) {
        List<GameList> userGames = gameListRepository.findByUserIdAndListType(userId, listType);
        return userGames.stream()
                .map(gameEntry -> gameAPIService.getGame(gameEntry.getGameId(), userId))
                .toList();
    }

    /**
     * Retrieves all categorized game lists for a specific user.
     * This includes the wishlist, games to play, and completed games.
     *
     * @param userId The unique identifier of the user.
     * @return A {@link GameListResponseDTO} containing all user lists.
     * @throws UserNotFoundException if the user does not exist.
     */
    public GameListResponseDTO getLists(Long userId) {
        var userExists = internalServiceClient.checkIfUserExists(userId);

        if (!userExists) throw new UserNotFoundException("User not found");

        return GameListResponseDTO.builder()
                .wishlist(gameListCreator(userId, ListType.WISHLIST))
                .ownedGames(gameListCreator(userId, ListType.OWNED))
                .completedGames(gameListCreator(userId, ListType.COMPLETED))
                .build();
    }

    private void sendDashboardEvent(Long userId, GameDetailsDTO game, EventType eventType, ListType oldListType) {
        IntegrationEvent<?> event;

        if (eventType == EventType.GAME_MOVED_BETWEEN_LISTS) {
            GameMovedBetweenListsPayload payload = GameMovedBetweenListsPayload.builder()
                    .gameId(game.guid())
                    .gameTitle(game.name())
                    .fromList(oldListType.name())
                    .toList(game.listType().name())
                    .build();
            event = new IntegrationEvent<>(eventType, "game-service", userId, LocalDateTime.now(), payload);
        } else {
            var type = EventType.GAME_ADDED_TO_LIST == eventType ? game.listType() : oldListType;
            GameToListPayload payload = GameToListPayload.builder()
                    .gameId(game.guid())
                    .gameTitle(game.name())
                    .listType(type.name())
                    .build();
            event = new IntegrationEvent<>(eventType, "game-service", userId, LocalDateTime.now(), payload);
        }
        rabbitService.sendDashboardEvent(event);
    }
}