package io.kk.userinsightsservice.model.mongo;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "library_dashboard")
public class DashboardDocument {
    @Id
    private String id;

    private Long userId;
    private Instant updatedAt;
    private Long version;

    private DashboardStats stats;
    private DashboardListsPreview listsPreview;
    private List<DashboardActivityItem> recentActivity;
    private List<DashboardReviewItem> latestReviews;
}
