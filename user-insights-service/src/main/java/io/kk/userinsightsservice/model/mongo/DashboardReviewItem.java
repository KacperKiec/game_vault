package io.kk.userinsightsservice.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DashboardReviewItem {
    private Long reviewId;
    private Long gameId;
    private String gameTitle;
    private Integer rating;
    private String reviewPreview;
    private LocalDate createdAt;
    private Boolean isDeleted;

    public void markAsDeleted() {
        isDeleted = true;
    }
}
