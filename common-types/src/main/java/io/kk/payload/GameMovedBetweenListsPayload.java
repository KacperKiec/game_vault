package io.kk.payload;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GameMovedBetweenListsPayload {
    private Long gameId;
    private String gameTitle;
    private String fromList;
    private String toList;
}
