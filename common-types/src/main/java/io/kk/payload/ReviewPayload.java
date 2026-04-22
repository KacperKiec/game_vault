package io.kk.payload;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class ReviewPayload {
    private Long reviewId;
    private Long gameId;
    private String gameTitle;
    private Integer rating;
    private String reviewPreview;
    private LocalDateTime createdAt;
}
