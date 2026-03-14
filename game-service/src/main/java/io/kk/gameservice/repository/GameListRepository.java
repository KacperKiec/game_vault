package io.kk.gameservice.repository;

import io.kk.gameservice.model.GameList;
import io.kk.gameservice.model.ListType;

import java.util.Optional;

public interface GameListRepository {
    GameList save(GameList gameList);
    Optional<GameList> findByUserIdAndListType(Long userId, ListType listType);
    Optional<GameList> findByUserIdAndGameId(Long userId, Long gameId);
    void delete(GameList gameList);
}
