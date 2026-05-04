package io.kk.userinsightsservice.service;

import io.kk.type.EventType;
import io.kk.userinsightsservice.model.mongo.DashboardDocument;
import io.kk.userinsightsservice.model.postgres.Activity;
import io.kk.userinsightsservice.repository.ActivityRepository;
import io.kk.userinsightsservice.repository.DashboardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RebuildServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private DashboardRepository dashboardRepository;

    @InjectMocks
    private RebuildService rebuildService;

    // --- empty activities ---

    @Test
    void rebuildDashboard_noActivities_doesNotSave() {
        when(activityRepository.findByUserIdOrderByOccurredAtAsc(1L)).thenReturn(List.of());

        rebuildService.rebuildDashboard(1L);

        verify(dashboardRepository, never()).save(any());
    }

    @Test
    void rebuildDashboard_noActivities_doesNotDelete() {
        when(activityRepository.findByUserIdOrderByOccurredAtAsc(1L)).thenReturn(List.of());

        rebuildService.rebuildDashboard(1L);

        verify(dashboardRepository, never()).deleteByUserId(any());
    }

    // --- USER_REGISTERED ---

    @Test
    void rebuildDashboard_userRegistered_setsUsernameAndEmail() {
        when(activityRepository.findByUserIdOrderByOccurredAtAsc(1L)).thenReturn(List.of(
                registeredActivity(1L)
        ));
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        rebuildService.rebuildDashboard(1L);

        verify(dashboardRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("alice");
        assertThat(captor.getValue().getEmail()).isEqualTo("alice@test.com");
    }

    @Test
    void rebuildDashboard_userRegistered_setsVersionToOne() {
        when(activityRepository.findByUserIdOrderByOccurredAtAsc(1L)).thenReturn(List.of(
                registeredActivity(1L)
        ));
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        rebuildService.rebuildDashboard(1L);

        verify(dashboardRepository).save(captor.capture());
        assertThat(captor.getValue().getVersion()).isEqualTo(1L);
    }

    // --- GAME_ADDED_TO_LIST ---

    @Test
    void rebuildDashboard_gameAddedToWishlist_incrementsWishlistCount() {
        when(activityRepository.findByUserIdOrderByOccurredAtAsc(1L)).thenReturn(List.of(
                registeredActivity(1L),
                buildActivity(1L, EventType.GAME_ADDED_TO_LIST, 42L, "Witcher", Map.of("listType", "WISHLIST"))
        ));
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        rebuildService.rebuildDashboard(1L);

        verify(dashboardRepository).save(captor.capture());
        assertThat(captor.getValue().getStats().getWishlistCount()).isEqualTo(1);
        assertThat(captor.getValue().getListsPreview().getWishlist()).hasSize(1);
    }

    @Test
    void rebuildDashboard_gameAddedToOwned_incrementsPlayingCount() {
        when(activityRepository.findByUserIdOrderByOccurredAtAsc(1L)).thenReturn(List.of(
                registeredActivity(1L),
                buildActivity(1L, EventType.GAME_ADDED_TO_LIST, 42L, "Witcher", Map.of("listType", "OWNED"))
        ));
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        rebuildService.rebuildDashboard(1L);

        verify(dashboardRepository).save(captor.capture());
        assertThat(captor.getValue().getStats().getPlayingCount()).isEqualTo(1);
    }

    @Test
    void rebuildDashboard_gameAddedToCompleted_incrementsCompletedCount() {
        when(activityRepository.findByUserIdOrderByOccurredAtAsc(1L)).thenReturn(List.of(
                registeredActivity(1L),
                buildActivity(1L, EventType.GAME_ADDED_TO_LIST, 42L, "Witcher", Map.of("listType", "COMPLETED"))
        ));
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        rebuildService.rebuildDashboard(1L);

        verify(dashboardRepository).save(captor.capture());
        assertThat(captor.getValue().getStats().getCompletedCount()).isEqualTo(1);
    }

    // --- GAME_REMOVED_FROM_LIST ---

    @Test
    void rebuildDashboard_gameRemovedFromWishlist_decrementsCountAndRemovesFromPreview() {
        when(activityRepository.findByUserIdOrderByOccurredAtAsc(1L)).thenReturn(List.of(
                registeredActivity(1L),
                buildActivity(1L, EventType.GAME_ADDED_TO_LIST, 42L, "Witcher", Map.of("listType", "WISHLIST")),
                buildActivity(1L, EventType.GAME_REMOVED_FROM_LIST, 42L, "Witcher", Map.of("listType", "WISHLIST"))
        ));
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        rebuildService.rebuildDashboard(1L);

        verify(dashboardRepository).save(captor.capture());
        assertThat(captor.getValue().getStats().getWishlistCount()).isZero();
        assertThat(captor.getValue().getListsPreview().getWishlist()).isEmpty();
    }

    // --- GAME_MOVED_BETWEEN_LISTS ---

    @Test
    void rebuildDashboard_gameMovedFromWishlistToCompleted_updatesStats() {
        when(activityRepository.findByUserIdOrderByOccurredAtAsc(1L)).thenReturn(List.of(
                registeredActivity(1L),
                buildActivity(1L, EventType.GAME_ADDED_TO_LIST, 42L, "Witcher", Map.of("listType", "WISHLIST")),
                buildActivity(1L, EventType.GAME_MOVED_BETWEEN_LISTS, 42L, "Witcher", Map.of("fromList", "WISHLIST", "toList", "COMPLETED"))
        ));
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        rebuildService.rebuildDashboard(1L);

        verify(dashboardRepository).save(captor.capture());
        var stats = captor.getValue().getStats();
        assertThat(stats.getWishlistCount()).isZero();
        assertThat(stats.getCompletedCount()).isEqualTo(1);
    }

    @Test
    void rebuildDashboard_gameMovedBetweenLists_appearsInTargetListPreview() {
        when(activityRepository.findByUserIdOrderByOccurredAtAsc(1L)).thenReturn(List.of(
                registeredActivity(1L),
                buildActivity(1L, EventType.GAME_ADDED_TO_LIST, 42L, "Witcher", Map.of("listType", "WISHLIST")),
                buildActivity(1L, EventType.GAME_MOVED_BETWEEN_LISTS, 42L, "Witcher", Map.of("fromList", "WISHLIST", "toList", "COMPLETED"))
        ));
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        rebuildService.rebuildDashboard(1L);

        verify(dashboardRepository).save(captor.capture());
        var lists = captor.getValue().getListsPreview();
        assertThat(lists.getWishlist()).isEmpty();
        assertThat(lists.getCompleted()).hasSize(1);
        assertThat(lists.getCompleted().getFirst().getGameId()).isEqualTo(42L);
    }

    // --- REVIEW_ADDED ---

    @Test
    void rebuildDashboard_reviewAdded_addsToLatestReviewsWithCorrectFields() {
        when(activityRepository.findByUserIdOrderByOccurredAtAsc(1L)).thenReturn(List.of(
                registeredActivity(1L),
                buildActivity(1L, EventType.REVIEW_ADDED, 5L, "Game", Map.of("reviewId", 10L, "rating", 4))
        ));
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        rebuildService.rebuildDashboard(1L);

        verify(dashboardRepository).save(captor.capture());
        var review = captor.getValue().getLatestReviews().getFirst();
        assertThat(review.getReviewId()).isEqualTo(10L);
        assertThat(review.getGameId()).isEqualTo(5L);
        assertThat(review.getRating()).isEqualTo(4);
    }

    @Test
    void rebuildDashboard_reviewAdded_setsEmptyPreview() {
        when(activityRepository.findByUserIdOrderByOccurredAtAsc(1L)).thenReturn(List.of(
                registeredActivity(1L),
                buildActivity(1L, EventType.REVIEW_ADDED, 5L, "Game", Map.of("reviewId", 10L, "rating", 4))
        ));
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        rebuildService.rebuildDashboard(1L);

        verify(dashboardRepository).save(captor.capture());
        assertThat(captor.getValue().getLatestReviews().getFirst().getReviewPreview()).isEmpty();
    }

    @Test
    void rebuildDashboard_reviewAdded_updatesReviewCountAndRatingSum() {
        when(activityRepository.findByUserIdOrderByOccurredAtAsc(1L)).thenReturn(List.of(
                registeredActivity(1L),
                buildActivity(1L, EventType.REVIEW_ADDED, 5L, "Game", Map.of("reviewId", 10L, "rating", 4))
        ));
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        rebuildService.rebuildDashboard(1L);

        verify(dashboardRepository).save(captor.capture());
        var stats = captor.getValue().getStats();
        assertThat(stats.getReviewCount()).isEqualTo(1);
        assertThat(stats.getAverageRating()).isEqualTo(4.0);
    }

    @Test
    void rebuildDashboard_latestReviews_atCapacity_dropsOldest() {
        var acts = new ArrayList<Activity>();
        acts.add(registeredActivity(1L));
        for (long i = 1; i <= 7; i++) {
            acts.add(buildActivity(1L, EventType.REVIEW_ADDED, 5L, "Game", Map.of("reviewId", i, "rating", 3)));
        }
        when(activityRepository.findByUserIdOrderByOccurredAtAsc(1L)).thenReturn(acts);
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        rebuildService.rebuildDashboard(1L);

        verify(dashboardRepository).save(captor.capture());
        var reviews = captor.getValue().getLatestReviews();
        assertThat(reviews).hasSize(6);
        assertThat(reviews.getFirst().getReviewId()).isEqualTo(2L);
        assertThat(reviews.getLast().getReviewId()).isEqualTo(7L);
    }

    // --- REVIEW_DELETED ---

    @Test
    void rebuildDashboard_reviewDeleted_marksReviewAsDeleted() {
        when(activityRepository.findByUserIdOrderByOccurredAtAsc(1L)).thenReturn(List.of(
                registeredActivity(1L),
                buildActivity(1L, EventType.REVIEW_ADDED, 5L, "Game", Map.of("reviewId", 10L, "rating", 4)),
                buildActivity(1L, EventType.REVIEW_DELETED, 5L, "Game", Map.of("reviewId", 10L, "rating", 4))
        ));
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        rebuildService.rebuildDashboard(1L);

        verify(dashboardRepository).save(captor.capture());
        var review = captor.getValue().getLatestReviews().stream()
                .filter(r -> r.getReviewId().equals(10L))
                .findFirst().orElseThrow();
        assertThat(review.getIsDeleted()).isTrue();
    }

    @Test
    void rebuildDashboard_reviewDeleted_decrementsReviewCount() {
        when(activityRepository.findByUserIdOrderByOccurredAtAsc(1L)).thenReturn(List.of(
                registeredActivity(1L),
                buildActivity(1L, EventType.REVIEW_ADDED, 5L, "Game", Map.of("reviewId", 10L, "rating", 4)),
                buildActivity(1L, EventType.REVIEW_DELETED, 5L, "Game", Map.of("reviewId", 10L, "rating", 4))
        ));
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        rebuildService.rebuildDashboard(1L);

        verify(dashboardRepository).save(captor.capture());
        assertThat(captor.getValue().getStats().getReviewCount()).isZero();
        assertThat(captor.getValue().getStats().getAverageRating()).isZero();
    }

    // --- version ---

    @Test
    void rebuildDashboard_versionEqualsNumberOfActivities() {
        when(activityRepository.findByUserIdOrderByOccurredAtAsc(1L)).thenReturn(List.of(
                registeredActivity(1L),
                buildActivity(1L, EventType.GAME_ADDED_TO_LIST, 1L, "Game A", Map.of("listType", "WISHLIST")),
                buildActivity(1L, EventType.GAME_ADDED_TO_LIST, 2L, "Game B", Map.of("listType", "OWNED"))
        ));
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        rebuildService.rebuildDashboard(1L);

        verify(dashboardRepository).save(captor.capture());
        assertThat(captor.getValue().getVersion()).isEqualTo(3L);
    }

    // --- lastProcessedEventId ---

    @Test
    void rebuildDashboard_setsLastProcessedEventIdToLastActivityEventId() {
        var lastActivity = buildActivity(1L, EventType.GAME_ADDED_TO_LIST, 42L, "Witcher", Map.of("listType", "WISHLIST"));
        when(activityRepository.findByUserIdOrderByOccurredAtAsc(1L)).thenReturn(List.of(
                registeredActivity(1L),
                lastActivity
        ));
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        rebuildService.rebuildDashboard(1L);

        verify(dashboardRepository).save(captor.capture());
        assertThat(captor.getValue().getLastProcessedEventId()).isEqualTo(lastActivity.getEventId());
    }

    // --- delete before save ---

    @Test
    void rebuildDashboard_callsDeleteBeforeSave() {
        when(activityRepository.findByUserIdOrderByOccurredAtAsc(1L)).thenReturn(List.of(
                registeredActivity(1L)
        ));
        var order = inOrder(dashboardRepository);

        rebuildService.rebuildDashboard(1L);

        order.verify(dashboardRepository).deleteByUserId(1L);
        order.verify(dashboardRepository).save(any());
    }

    // --- recentActivity cap ---

    @Test
    void rebuildDashboard_recentActivity_atCapacity_dropsOldest() {
        var acts = new ArrayList<Activity>();
        acts.add(registeredActivity(1L));
        for (int i = 1; i <= 11; i++) {
            acts.add(buildActivity(1L, EventType.GAME_ADDED_TO_LIST, (long) i, "Game " + i, Map.of("listType", "WISHLIST")));
        }
        when(activityRepository.findByUserIdOrderByOccurredAtAsc(1L)).thenReturn(acts);
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        rebuildService.rebuildDashboard(1L);

        verify(dashboardRepository).save(captor.capture());
        var recent = captor.getValue().getRecentActivity();
        assertThat(recent).hasSize(10);
        assertThat(recent.getFirst().getGameId()).isEqualTo(2L);
        assertThat(recent.getLast().getGameId()).isEqualTo(11L);
    }

    // --- helpers ---

    private Activity buildActivity(Long userId, EventType eventType, Long gameId, String gameName, Map<String, Object> metadata) {
        var activity = new Activity();
        activity.setUserId(userId);
        activity.setEventId(UUID.randomUUID());
        activity.setEventType(eventType);
        activity.setOccurredAt(LocalDateTime.now());
        activity.setRelatedGameId(gameId);
        activity.setRelatedGameName(gameName);
        activity.setMetadata(metadata);
        return activity;
    }

    private Activity registeredActivity(Long userId) {
        return buildActivity(userId, EventType.USER_REGISTERED, -1L, "",
                Map.of("username", "alice", "email", "alice@test.com"));
    }
}