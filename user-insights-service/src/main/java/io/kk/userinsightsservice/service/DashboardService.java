package io.kk.userinsightsservice.service;

import io.kk.envelope.IntegrationEvent;
import io.kk.payload.GameMovedBetweenListsPayload;
import io.kk.payload.GameToListPayload;
import io.kk.payload.ReviewPayload;
import io.kk.payload.UserRegisteredPayload;
import io.kk.type.EventType;
import io.kk.userinsightsservice.exception.DashboardException;
import io.kk.userinsightsservice.model.mongo.*;
import io.kk.userinsightsservice.repository.DashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

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

    public DashboardDocument getUserDashboard(Long userId) {
        return dashboardRepository.findByUserId(userId).orElseThrow(
                () -> new DashboardException("Dashboard not found for user: " + userId)
        );
    }

    public Boolean isEventApplied(long userId, UUID eventId) {
        var dashboard = dashboardRepository.findByUserId(userId).orElseThrow(
                () -> new DashboardException("Dashboard not found for user: " + userId)
        );
        return dashboard.getLastProcessedEventId().equals(eventId);
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

        dashboard.setStats(new DashboardStats(0, 0, 0, 0, 0.0));
        dashboard.setListsPreview(new DashboardListsPreview(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        dashboard.setRecentActivity(new ArrayList<>());
        dashboard.setLatestReviews(new ArrayList<>());

        dashboard.setLastProcessedEventId(event.getEventId());
        dashboardRepository.save(dashboard);
    }

    private void addReview(IntegrationEvent<?> event) {
        var dashboard = getUserDashboard(event.getUserId());
        dashboard.incrementVersion();

        if (event.getPayload() instanceof ReviewPayload payload) {
            var reviews = dashboard.getLatestReviews();
            if (reviews.size() >= 6) reviews.removeFirst();
            reviews.add(new DashboardReviewItem(
                    payload.getReviewId(),
                    payload.getGameId(),
                    payload.getGameTitle(),
                    payload.getRating(),
                    payload.getReviewPreview(),
                    payload.getCreatedAt(),
                    false));

            var stats = dashboard.getStats();
            stats.setReviewCount(stats.getReviewCount() + 1);
            stats.setRatingSum(stats.getRatingSum() + payload.getRating());

            appendRecentActivity(dashboard, event.getEventType(), event.getOccurredAt(),
                    payload.getGameId(), payload.getGameTitle(),
                    Map.of("reviewId", payload.getReviewId(), "rating", payload.getRating()));
        }

        dashboard.setLastProcessedEventId(event.getEventId());
        dashboardRepository.save(dashboard);
    }

    private void removeReview(IntegrationEvent<?> event) {
        var dashboard = getUserDashboard(event.getUserId());
        dashboard.incrementVersion();

        if (event.getPayload() instanceof ReviewPayload payload) {
            dashboard.getLatestReviews().stream()
                    .filter(r -> r.getReviewId().equals(payload.getReviewId()))
                    .findFirst()
                    .ifPresent(DashboardReviewItem::markAsDeleted);

            var stats = dashboard.getStats();
            stats.setReviewCount(stats.getReviewCount() - 1);
            stats.setRatingSum(stats.getRatingSum() - payload.getRating());

            appendRecentActivity(dashboard, event.getEventType(), event.getOccurredAt(),
                    payload.getGameId(), payload.getGameTitle(),
                    Map.of("reviewId", payload.getReviewId()));
        }

        dashboard.setLastProcessedEventId(event.getEventId());
        dashboardRepository.save(dashboard);
    }

    private void addGameToList(IntegrationEvent<?> event) {
        var dashboard = getUserDashboard(event.getUserId());
        dashboard.incrementVersion();

        if (event.getPayload() instanceof GameToListPayload payload) {
            var preview = new DashboardGamePreview();
            preview.setGameId(payload.getGameId());
            preview.setTitle(payload.getGameTitle());

            var listsPreview = dashboard.getListsPreview();
            var stats = dashboard.getStats();

            switch (payload.getListType()) {
                case "WISHLIST" -> { listsPreview.getWishlist().add(preview); stats.setWishlistCount(stats.getWishlistCount() + 1); }
                case "OWNED" -> { listsPreview.getPlaying().add(preview);  stats.setPlayingCount(stats.getPlayingCount() + 1); }
                case "COMPLETED" -> { listsPreview.getCompleted().add(preview); stats.setCompletedCount(stats.getCompletedCount() + 1); }
            }

            appendRecentActivity(dashboard, event.getEventType(), event.getOccurredAt(),
                    payload.getGameId(), payload.getGameTitle(),
                    Map.of("listType", payload.getListType()));
        }

        dashboard.setLastProcessedEventId(event.getEventId());
        dashboardRepository.save(dashboard);
    }

    private void removeGameFromList(IntegrationEvent<?> event) {
        var dashboard = getUserDashboard(event.getUserId());
        dashboard.incrementVersion();

        if (event.getPayload() instanceof GameToListPayload payload) {
            var listsPreview = dashboard.getListsPreview();
            var stats = dashboard.getStats();

            switch (payload.getListType()) {
                case "WISHLIST" -> { if (listsPreview.getWishlist().removeIf(g -> g.getGameId().equals(payload.getGameId()))) stats.setWishlistCount(stats.getWishlistCount() - 1); }
                case "OWNED" -> { if (listsPreview.getPlaying().removeIf(g -> g.getGameId().equals(payload.getGameId())))  stats.setPlayingCount(stats.getPlayingCount() - 1); }
                case "COMPLETED" -> { if (listsPreview.getCompleted().removeIf(g -> g.getGameId().equals(payload.getGameId()))) stats.setCompletedCount(stats.getCompletedCount() - 1); }
            }

            appendRecentActivity(dashboard, event.getEventType(), event.getOccurredAt(),
                    payload.getGameId(), payload.getGameTitle(),
                    Map.of("listType", payload.getListType()));
        }

        dashboard.setLastProcessedEventId(event.getEventId());
        dashboardRepository.save(dashboard);
    }

    private void updateGameInList(IntegrationEvent<?> event) {
        var dashboard = getUserDashboard(event.getUserId());
        dashboard.incrementVersion();

        if (event.getPayload() instanceof GameMovedBetweenListsPayload payload) {
            var preview = new DashboardGamePreview();
            preview.setGameId(payload.getGameId());
            preview.setTitle(payload.getGameTitle());

            var listsPreview = dashboard.getListsPreview();
            var stats = dashboard.getStats();

            switch (payload.getFromList()) {
                case "WISHLIST" -> { if (listsPreview.getWishlist().removeIf(g -> g.getGameId().equals(payload.getGameId()))) stats.setWishlistCount(stats.getWishlistCount() - 1); }
                case "OWNED" -> { if (listsPreview.getPlaying().removeIf(g -> g.getGameId().equals(payload.getGameId())))  stats.setPlayingCount(stats.getPlayingCount() - 1); }
                case "COMPLETED" -> { if (listsPreview.getCompleted().removeIf(g -> g.getGameId().equals(payload.getGameId()))) stats.setCompletedCount(stats.getCompletedCount() - 1); }
            }

            switch (payload.getToList()) {
                case "WISHLIST" -> { listsPreview.getWishlist().add(preview); stats.setWishlistCount(stats.getWishlistCount() + 1); }
                case "OWNED" -> { listsPreview.getPlaying().add(preview);  stats.setPlayingCount(stats.getPlayingCount() + 1); }
                case "COMPLETED" -> { listsPreview.getCompleted().add(preview); stats.setCompletedCount(stats.getCompletedCount() + 1); }
            }

            appendRecentActivity(dashboard, event.getEventType(), event.getOccurredAt(),
                    payload.getGameId(), payload.getGameTitle(),
                    Map.of("fromList", payload.getFromList(), "toList", payload.getToList()));
        }

        dashboard.setLastProcessedEventId(event.getEventId());
        dashboardRepository.save(dashboard);
    }

    private void appendRecentActivity(DashboardDocument dashboard, EventType type, LocalDateTime occurredAt,
                                      Long gameId, String gameTitle, Map<String, Object> details) {
        DashboardActivityItem item = new DashboardActivityItem();
        item.setType(type);
        item.setOccurredAt(occurredAt);
        item.setGameId(gameId);
        item.setGameTitle(gameTitle);
        item.setDetails(details);

        var activity = dashboard.getRecentActivity();
        if (activity.size() >= 10) activity.removeFirst();
        activity.add(item);
    }
}