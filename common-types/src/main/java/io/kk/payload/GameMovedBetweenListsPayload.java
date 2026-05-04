package io.kk.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GameMovedBetweenListsPayload {
    private Long gameId;
    private String gameTitle;
    private String fromList;
    private String toList;
}
