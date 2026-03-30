package io.kk.gameservice.repository;

import io.kk.gameservice.model.GameList;
import io.kk.gameservice.model.ListType;

import java.util.List;
import java.util.Optional;

public interface GameListRepository {
    GameList save(GameList gameList);
    List<GameList> findByUserIdAndListType(Long userId, ListType listType);
    Optional<GameList> findByUserIdAndGameId(Long userId, Long gameId);
    List<GameList> findByGameId(Long gameId);
    void delete(GameList gameList);
}
