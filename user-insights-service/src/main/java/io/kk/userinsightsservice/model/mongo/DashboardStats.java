package io.kk.userinsightsservice.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStats {
    private int wishlistCount;
    private int playingCount;
    private int completedCount;
    private int reviewCount;
    private double ratingSum;

    public double getAverageRating() {
        return reviewCount == 0 ? 0.0 : ratingSum / reviewCount;
    }
}
