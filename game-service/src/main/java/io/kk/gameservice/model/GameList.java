package io.kk.gameservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameList {

    private Long userId;
    private Long gameId;
    private ListType listType;
}
