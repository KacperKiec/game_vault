package io.kk.gameservice.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "game_list")
@IdClass(GameListId.class)
public class GameList {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Enumerated(EnumType.STRING)
    @Column(name = "list_type", nullable = false)
    private ListType listType;
}
