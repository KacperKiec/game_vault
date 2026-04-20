package io.kk.payload;

import lombok.Builder;

@Builder
public class GameMovedBetweenListsPayload {
    private Long gameId;
    private String gameTitle;
    private String coverUrl;
    private String fromList;
    private String toList;
}
