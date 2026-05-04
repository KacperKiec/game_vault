package io.kk.userinsightsservice.service;

import io.kk.type.EventType;
import io.kk.userinsightsservice.model.mongo.*;
import io.kk.userinsightsservice.model.postgres.Activity;
import io.kk.userinsightsservice.repository.ActivityRepository;
import io.kk.userinsightsservice.repository.DashboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RebuildService {

    private final ActivityRepository activityRepository;
    private final DashboardRepository dashboardRepository;

    public void rebuildDashboard(Long userId) {
        List<Activity> activities = activityRepository.findByUserIdOrderByOccurredAtAsc(userId);
        if (activities.isEmpty()) return;

        log.warn("Rebuilding dashboard for userId={} from {} activities", userId, activities.size());

        dashboardRepository.deleteByUserId(userId);

        DashboardDocument dashboard = new DashboardDocument();
        dashboard.setUserId(userId);
        dashboard.setVersion(0L);
        dashboard.setStats(new DashboardStats(0, 0, 0, 0, 0.0));
        dashboard.setListsPreview(new DashboardListsPreview(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        dashboard.setRecentActivity(new ArrayList<>());
        dashboard.setLatestReviews(new ArrayList<>());

        for (Activity activity : activities) {
            applyActivity(dashboard, activity);
        }

        dashboard.setUpdatedAt(LocalDateTime.now());
        dashboard.setLastProcessedEventId(activities.getLast().getEventId());
        dashboardRepository.save(dashboard);

        log.info("Dashboard rebuilt for userId={}, version={}", userId, dashboard.getVersion());
    }

    private void applyActivity(DashboardDocument dashboard, Activity activity) {
        var stats = dashboard.getStats();
        var listsPreview = dashboard.getListsPreview();
        Map<String, Object> meta = activity.getMetadata();

        switch (activity.getEventType()) {
            case USER_REGISTERED -> {
                dashboard.setUsername((String) meta.getOrDefault("username", ""));
                dashboard.setEmail((String) meta.getOrDefault("email", ""));
                dashboard.incrementVersion();
            }
            case GAME_ADDED_TO_LIST -> {
                var preview = gamePreview(activity);

                switch ((String) meta.getOrDefault("listType", "")) {
                    case "WISHLIST" -> { listsPreview.getWishlist().add(preview);   stats.setWishlistCount(stats.getWishlistCount() + 1); }
                    case "OWNED" -> { listsPreview.getOwned().add(preview);    stats.setPlayingCount(stats.getPlayingCount() + 1); }
                    case "COMPLETED" -> { listsPreview.getCompleted().add(preview);  stats.setCompletedCount(stats.getCompletedCount() + 1); }
                }
                appendRecentActivity(dashboard, activity.getEventType(), activity.getOccurredAt(),
                        activity.getRelatedGameId(), activity.getRelatedGameName(),
                        Map.of("listType", meta.getOrDefault("listType", "")));
                dashboard.incrementVersion();
            }
            case GAME_REMOVED_FROM_LIST -> {
                var gameId = activity.getRelatedGameId();

                switch ((String) meta.getOrDefault("listType", "")) {
                    case "WISHLIST" -> { if (listsPreview.getWishlist().removeIf(g -> g.getGameId().equals(gameId)))  stats.setWishlistCount(stats.getWishlistCount() - 1); }
                    case "OWNED" -> { if (listsPreview.getOwned().removeIf(g -> g.getGameId().equals(gameId)))   stats.setPlayingCount(stats.getPlayingCount() - 1); }
                    case "COMPLETED" -> { if (listsPreview.getCompleted().removeIf(g -> g.getGameId().equals(gameId))) stats.setCompletedCount(stats.getCompletedCount() - 1); }
                }
                appendRecentActivity(dashboard, activity.getEventType(), activity.getOccurredAt(),
                        activity.getRelatedGameId(), activity.getRelatedGameName(),
                        Map.of("listType", meta.getOrDefault("listType", "")));
                dashboard.incrementVersion();
            }
            case GAME_MOVED_BETWEEN_LISTS -> {
                var gameId = activity.getRelatedGameId();
                var preview = gamePreview(activity);
                String from = (String) meta.getOrDefault("fromList", "");
                String to = (String) meta.getOrDefault("toList", "");

                switch (from) {
                    case "WISHLIST" -> { if (listsPreview.getWishlist().removeIf(g -> g.getGameId().equals(gameId)))  stats.setWishlistCount(stats.getWishlistCount() - 1); }
                    case "OWNED" -> { if (listsPreview.getOwned().removeIf(g -> g.getGameId().equals(gameId)))   stats.setPlayingCount(stats.getPlayingCount() - 1); }
                    case "COMPLETED" -> { if (listsPreview.getCompleted().removeIf(g -> g.getGameId().equals(gameId))) stats.setCompletedCount(stats.getCompletedCount() - 1); }
                }
                switch (to) {
                    case "WISHLIST" -> { listsPreview.getWishlist().add(preview);   stats.setWishlistCount(stats.getWishlistCount() + 1); }
                    case "OWNED" -> { listsPreview.getOwned().add(preview);    stats.setPlayingCount(stats.getPlayingCount() + 1); }
                    case "COMPLETED" -> { listsPreview.getCompleted().add(preview);  stats.setCompletedCount(stats.getCompletedCount() + 1); }
                }

                appendRecentActivity(dashboard, activity.getEventType(), activity.getOccurredAt(),
                        activity.getRelatedGameId(), activity.getRelatedGameName(),
                        Map.of("fromList", from, "toList", to));
                dashboard.incrementVersion();
            }
            case REVIEW_ADDED -> {
                var reviewId = toLong(meta.get("reviewId"));
                int rating = toInt(meta.get("rating"));
                var reviews = dashboard.getLatestReviews();

                if (reviews.size() >= 6) reviews.removeFirst();
                reviews.add(new DashboardReviewItem(
                        reviewId,
                        activity.getRelatedGameId(),
                        activity.getRelatedGameName(),
                        rating,
                        "",
                        activity.getOccurredAt(),
                        false));

                stats.setReviewCount(stats.getReviewCount() + 1);
                stats.setRatingSum(stats.getRatingSum() + rating);

                appendRecentActivity(dashboard, activity.getEventType(), activity.getOccurredAt(),
                        activity.getRelatedGameId(), activity.getRelatedGameName(),
                        Map.of("reviewId", reviewId, "rating", rating));
                dashboard.incrementVersion();
            }
            case REVIEW_DELETED -> {
                var reviewId = toLong(meta.get("reviewId"));
                int rating = toInt(meta.get("rating"));

                dashboard.getLatestReviews().stream()
                        .filter(r -> r.getReviewId().equals(reviewId))
                        .findFirst()
                        .ifPresent(DashboardReviewItem::markAsDeleted);

                stats.setReviewCount(stats.getReviewCount() - 1);
                stats.setRatingSum(stats.getRatingSum() - rating);

                appendRecentActivity(dashboard, activity.getEventType(), activity.getOccurredAt(),
                        activity.getRelatedGameId(), activity.getRelatedGameName(),
                        Map.of("reviewId", reviewId));
                dashboard.incrementVersion();
            }
        }
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

    private DashboardGamePreview gamePreview(Activity activity) {
        var preview = new DashboardGamePreview();
        preview.setGameId(activity.getRelatedGameId());
        preview.setTitle(activity.getRelatedGameName());
        return preview;
    }

    private Long toLong(Object value) {
        return switch (value) {
            case null -> 0L;
            case Long l -> l;
            case Integer i -> i.longValue();
            case Number n -> n.longValue();
            default -> Long.valueOf(value.toString());
        };
    }

    private int toInt(Object value) {
        return switch (value) {
            case null -> 0;
            case Integer i -> i;
            case Number n -> n.intValue();
            default -> Integer.parseInt(value.toString());
        };
    }
}