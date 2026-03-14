package io.kk.gameservice.repository.impl;

import io.kk.gameservice.model.GameList;
import io.kk.gameservice.model.GameListId;
import io.kk.gameservice.repository.GameListRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaGameListRepository extends JpaRepository<GameList, GameListId>, GameListRepository {

}
