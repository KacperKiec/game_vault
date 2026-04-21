package io.kk.payload;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GameToListPayload {
    private Long gameId;
    private String gameTitle;
    private String listType;
}
