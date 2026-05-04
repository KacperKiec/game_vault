package io.kk.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewPayload {
    private Long reviewId;
    private Long gameId;
    private String gameTitle;
    private Integer rating;
    private String reviewPreview;
    private LocalDateTime createdAt;
}
