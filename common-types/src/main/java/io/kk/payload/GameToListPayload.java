package io.kk.payload;

import lombok.Builder;

@Builder
public class GameToListPayload {
    private Long gameId;
    private String gameTitle;
    private String coverUrl;
    private String listType;
}
