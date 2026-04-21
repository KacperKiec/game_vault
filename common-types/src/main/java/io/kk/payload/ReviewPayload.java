package io.kk.payload;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
public class ReviewPayload {
    private Long reviewId;
    private Long gameId;
    private String gameTitle;
    private Integer rating;
    private String reviewPreview;
    private LocalDate createdAt;
}
