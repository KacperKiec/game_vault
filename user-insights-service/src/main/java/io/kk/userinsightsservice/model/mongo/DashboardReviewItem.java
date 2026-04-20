package io.kk.userinsightsservice.model.mongo;

import lombok.Data;

import java.time.Instant;

@Data
public class DashboardReviewItem {
    private Long reviewId;
    private Long gameId;
    private String gameTitle;
    private String coverUrl;
    private Integer rating;
    private String reviewPreview;
    private Instant createdAt;
}
