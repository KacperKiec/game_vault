package io.kk.payload;

import lombok.Builder;

@Builder
public class ReviewPayload {
    private Long reviewId;
    private Long gameId;
    private String gameTitle;
    private String coverUrl;
    private Integer rating;
    private String reviewPreview;
}
