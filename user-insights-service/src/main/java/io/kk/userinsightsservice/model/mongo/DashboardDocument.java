package io.kk.userinsightsservice.model.mongo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "library_dashboard")
public class DashboardDocument {
    @Id
    private String id;

    @NotNull
    private Long userId;

    @NotNull
    private LocalDateTime updatedAt;

    @NotNull
    private Long version;

    @NotBlank
    private String username;

    @NotBlank
    private String email;

    private DashboardStats stats;
    private DashboardListsPreview listsPreview;
    private List<DashboardActivityItem> recentActivity;
    private List<DashboardReviewItem> latestReviews;

    public void incrementVersion() {
        version++;
    }
}
