package io.kk.userinsightsservice.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DashboardReviewItem {
    private Long reviewId;
    private Long gameId;
    private String gameTitle;
    private Integer rating;
    private String reviewPreview;
    private LocalDateTime createdAt;
    private Boolean isDeleted;

    public void markAsDeleted() {
        isDeleted = true;
    }
}
