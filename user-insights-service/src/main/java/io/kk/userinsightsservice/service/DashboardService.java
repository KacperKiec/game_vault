package io.kk.userinsightsservice.service;

import io.kk.envelope.IntegrationEvent;
import io.kk.payload.GameMovedBetweenListsPayload;
import io.kk.payload.GameToListPayload;
import io.kk.payload.ReviewPayload;
import io.kk.payload.UserRegisteredPayload;
import io.kk.userinsightsservice.dto.ActivityDTO;
import io.kk.userinsightsservice.exception.DashboardException;
import io.kk.userinsightsservice.model.mongo.*;
import io.kk.userinsightsservice.repository.DashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public void handleDashboardEvent(IntegrationEvent<?> event) {
        switch (event.getEventType()) {
            case USER_REGISTERED -> createDashboard(event);
            case REVIEW_ADDED -> addReview(event);
            case REVIEW_DELETED -> removeReview(event);
            case GAME_ADDED_TO_LIST -> addGameToList(event);
            case GAME_REMOVED_FROM_LIST -> removeGameFromList(event);
            case GAME_MOVED_BETWEEN_LISTS -> updateGameInList(event);
        }
    }

    public void updateRecentActivity(ActivityDTO dto) {
        var dashboard = getUserDashboard(dto.userId());
        dashboard.incrementVersion();

        DashboardActivityItem item = new DashboardActivityItem();
        item.setType(dto.activityType());
        item.setOccurredAt(dto.occurredAt());
        item.setGameId(dto.relatedGameId());
        item.setGameTitle((String) dto.metadata().get("gameName"));
        item.setDetails(dto.metadata());

        var activity = dashboard.getRecentActivity();
        if (activity.size() >= 10) {
            activity.removeFirst();
        }
        activity.add(item);

        dashboardRepository.save(dashboard);
    }

    public DashboardDocument getUserDashboard(Long userId) {
        return dashboardRepository.findByUserId(userId).orElseThrow(
                () -> new DashboardException("Dashboard not found for user: " + userId)
        );
    }

    private void createDashboard(IntegrationEvent<?> event) {
        DashboardDocument dashboard = new DashboardDocument();
        dashboard.setUserId(event.getUserId());
        dashboard.setVersion(1L);
        dashboard.setUpdatedAt(LocalDateTime.now());

        if (event.getPayload() instanceof UserRegisteredPayload payload) {
            dashboard.setUsername(payload.getUsername());
            dashboard.setEmail(payload.getEmail());
        }

        DashboardStats stats = new DashboardStats(0, 0, 0, 0, 0.0);
        dashboard.setStats(stats);

        DashboardListsPreview listsPreview = new DashboardListsPreview(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        dashboard.setListsPreview(listsPreview);

        dashboard.setRecentActivity(new ArrayList<>());
        dashboard.setLatestReviews(new ArrayList<>());

        dashboardRepository.save(dashboard);
    }

    private void addReview(IntegrationEvent<?> event) {
        var dashboard = getUserDashboard(event.getUserId());
        dashboard.incrementVersion();

        if (event.getPayload() instanceof ReviewPayload payload) {
            DashboardReviewItem item = new DashboardReviewItem(
                    payload.getReviewId(),
                    payload.getGameId(),
                    payload.getGameTitle(),
                    payload.getRating(),
                    payload.getReviewPreview(),
                    payload.getCreatedAt(),
                    false);

            var reviews = dashboard.getLatestReviews();
            if (reviews.size() < 6) {
                reviews.add(item);
            } else {
                reviews.removeFirst();
                reviews.add(item);
            }

            var stats = dashboard.getStats();
            stats.setReviewCount(stats.getReviewCount() + 1);
            stats.setRatingSum(stats.getRatingSum() + payload.getRating());
        }
        dashboardRepository.save(dashboard);
    }

    private void removeReview(IntegrationEvent<?> event) {
        var dashboard = getUserDashboard(event.getUserId());
        dashboard.incrementVersion();

        if (event.getPayload() instanceof ReviewPayload payload) {
            var reviews = dashboard.getLatestReviews();
            reviews.stream().filter(r -> r.getReviewId().equals(payload.getReviewId())).findFirst().ifPresent(DashboardReviewItem::markAsDeleted);

            var stats = dashboard.getStats();
            stats.setReviewCount(stats.getReviewCount() - 1);
            stats.setRatingSum(stats.getRatingSum() - payload.getRating());
        }

        dashboardRepository.save(dashboard);
    }

    private void addGameToList(IntegrationEvent<?> event) {
        var dashboard = getUserDashboard(event.getUserId());
        dashboard.incrementVersion();

        if (event.getPayload() instanceof GameToListPayload payload) {
            DashboardGamePreview preview = new DashboardGamePreview();
            preview.setGameId(payload.getGameId());
            preview.setTitle(payload.getGameTitle());

            var listsPreview = dashboard.getListsPreview();
            var stats = dashboard.getStats();

            switch (payload.getListType()) {
                case "WISHLIST" -> {
                    listsPreview.getWishlist().add(preview);
                    stats.setWishlistCount(stats.getWishlistCount() + 1);
                }
                case "OWNED" -> {
                    listsPreview.getPlaying().add(preview);
                    stats.setPlayingCount(stats.getPlayingCount() + 1);
                }
                case "COMPLETED" -> {
                    listsPreview.getCompleted().add(preview);
                    stats.setCompletedCount(stats.getCompletedCount() + 1);
                }
            }
        }
        dashboardRepository.save(dashboard);
    }

    private void removeGameFromList(IntegrationEvent<?> event) {
        var dashboard = getUserDashboard(event.getUserId());
        dashboard.incrementVersion();

        if (event.getPayload() instanceof GameToListPayload payload) {
            var listsPreview = dashboard.getListsPreview();
            var stats = dashboard.getStats();

            switch (payload.getListType()) {
                case "WISHLIST" -> {
                    if (listsPreview.getWishlist().removeIf(g -> g.getGameId().equals(payload.getGameId())))
                        stats.setWishlistCount(stats.getWishlistCount() - 1);
                }
                case "OWNED" -> {
                    if (listsPreview.getPlaying().removeIf(g -> g.getGameId().equals(payload.getGameId())))
                        stats.setPlayingCount(stats.getPlayingCount() - 1);
                }
                case "COMPLETED" -> {
                    if (listsPreview.getCompleted().removeIf(g -> g.getGameId().equals(payload.getGameId())))
                        stats.setCompletedCount(stats.getCompletedCount() - 1);
                }
            }
        }
        dashboardRepository.save(dashboard);
    }

    private void updateGameInList(IntegrationEvent<?> event) {
        var dashboard = getUserDashboard(event.getUserId());
        dashboard.incrementVersion();

        if (event.getPayload() instanceof GameMovedBetweenListsPayload payload) {
            DashboardGamePreview preview = new DashboardGamePreview();
            preview.setGameId(payload.getGameId());
            preview.setTitle(payload.getGameTitle());

            var listsPreview = dashboard.getListsPreview();
            var stats = dashboard.getStats();

            switch (payload.getFromList()) {
                case "WISHLIST" -> {
                    if (listsPreview.getWishlist().removeIf(g -> g.getGameId().equals(payload.getGameId())))
                        stats.setWishlistCount(stats.getWishlistCount() - 1);
                }
                case "OWNED" -> {
                    if (listsPreview.getPlaying().removeIf(g -> g.getGameId().equals(payload.getGameId())))
                        stats.setPlayingCount(stats.getPlayingCount() - 1);
                }
                case "COMPLETED" -> {
                    if (listsPreview.getCompleted().removeIf(g -> g.getGameId().equals(payload.getGameId())))
                        stats.setCompletedCount(stats.getCompletedCount() - 1);
                }
            }

            switch (payload.getToList()) {
                case "WISHLIST" -> {
                    listsPreview.getWishlist().add(preview);
                    stats.setWishlistCount(stats.getWishlistCount() + 1);
                }
                case "OWNED" -> {
                    listsPreview.getPlaying().add(preview);
                    stats.setPlayingCount(stats.getPlayingCount() + 1);
                }
                case "COMPLETED" -> {
                    listsPreview.getCompleted().add(preview);
                    stats.setCompletedCount(stats.getCompletedCount() + 1);
                }
            }
        }
        dashboardRepository.save(dashboard);
    }
}
