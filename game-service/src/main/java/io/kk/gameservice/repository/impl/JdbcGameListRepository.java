package io.kk.gameservice.repository.impl;

import io.kk.gameservice.model.GameList;
import io.kk.gameservice.model.ListType;
import io.kk.gameservice.repository.GameListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcGameListRepository implements GameListRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<GameList> gameListRowMapper = (rs, rowNum) -> GameList.builder()
            .userId(rs.getLong("user_id"))
            .gameId(rs.getLong("game_id"))
            .listType(ListType.valueOf(rs.getString("list_type")))
            .build();

    @Override
    public GameList save(GameList gameList) {
        Optional<GameList> existing = findByUserIdAndGameId(gameList.getUserId(), gameList.getGameId());

        if (existing.isPresent()) {
            String sql = "UPDATE game_list SET list_type = ? WHERE user_id = ? AND game_id = ?";
            jdbcTemplate.update(sql,
                    gameList.getListType().name(),
                    gameList.getUserId(),
                    gameList.getGameId()
            );
        } else {
            String sql = "INSERT INTO game_list (user_id, game_id, list_type) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql,
                    gameList.getUserId(),
                    gameList.getGameId(),
                    gameList.getListType().name()
            );
        }
        return gameList;
    }

    @Override
    public List<GameList> findByUserIdAndListType(Long userId, ListType listType) {
        String sql = "SELECT * FROM game_list WHERE user_id = ? AND list_type = ?";
        return jdbcTemplate.query(sql, gameListRowMapper, userId, listType.name());
    }

    @Override
    public Optional<GameList> findByUserIdAndGameId(Long userId, Long gameId) {
        String sql = "SELECT * FROM game_list WHERE user_id = ? AND game_id = ?";
        try {
            GameList result = jdbcTemplate.queryForObject(sql, gameListRowMapper, userId, gameId);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<GameList> findByGameId(Long gameId) {
        String sql = "SELECT * FROM game_list WHERE game_id = ?";
        return jdbcTemplate.query(sql, gameListRowMapper, gameId);
    }

    @Override
    public void delete(GameList gameList) {
        String sql = "DELETE FROM game_list WHERE user_id = ? AND game_id = ?";
        jdbcTemplate.update(sql, gameList.getUserId(), gameList.getGameId());
    }
}
