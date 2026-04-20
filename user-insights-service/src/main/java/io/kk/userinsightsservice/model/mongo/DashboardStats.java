package io.kk.userinsightsservice.model.mongo;

import lombok.Data;

@Data
public class DashboardStats {
    private Integer wishlistCount;
    private Integer playingCount;
    private Integer completedCount;
    private Integer reviewCount;
    private Double averageRating;
}
