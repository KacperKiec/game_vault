package io.kk.gameservice.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public class GameListId implements Serializable {

    private Long userId;
    private Long gameId;

    public GameListId() {}

    public GameListId(Long user, Long game) {
        this.userId = user;
        this.gameId = game;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameListId)) return false;
        GameListId that = (GameListId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(gameId, that.gameId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, gameId);
    }
}
