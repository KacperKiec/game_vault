package io.kk.userinsightsservice.service;

import io.kk.envelope.IntegrationEvent;
import io.kk.payload.ReviewPayload;
import io.kk.type.EventType;
import io.kk.userinsightsservice.model.mongo.DashboardReviewItem;
import io.kk.userinsightsservice.repository.DashboardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static io.kk.userinsightsservice.service.DashboardServiceCoreTest.buildDashboard;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceReviewTest {

    @Mock
    private DashboardRepository dashboardRepository;

    @InjectMocks
    private DashboardService dashboardService;

    // --- addReview ---

    @Test
    void addReview_addsItemToLatestReviews() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(reviewAddedEvent(1L, 10L, 8));

        assertThat(dashboard.getLatestReviews()).hasSize(1);
        assertThat(dashboard.getLatestReviews().getFirst().getReviewId()).isEqualTo(10L);
        assertThat(dashboard.getLatestReviews().getFirst().getGameTitle()).isEqualTo("Game");
    }

    @Test
    void addReview_updatesReviewCountAndAverageRating() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(reviewAddedEvent(1L, 10L, 8));

        assertThat(dashboard.getStats().getReviewCount()).isEqualTo(1);
        assertThat(dashboard.getStats().getAverageRating()).isEqualTo(8.0);
    }

    @Test
    void addReview_multipleReviews_computesCorrectAverage() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(reviewAddedEvent(1L, 10L, 6));
        dashboardService.handleDashboardEvent(reviewAddedEvent(1L, 11L, 10));

        assertThat(dashboard.getStats().getReviewCount()).isEqualTo(2);
        assertThat(dashboard.getStats().getAverageRating()).isEqualTo(8.0);
    }

    @Test
    void addReview_incrementsVersion() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(reviewAddedEvent(1L, 10L, 8));

        assertThat(dashboard.getVersion()).isEqualTo(2L);
    }

    @Test
    void addReview_whenListAtCapacity_removesOldestAndAddsNew() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        for (long i = 1; i <= 6; i++) {
            dashboardService.handleDashboardEvent(reviewAddedEvent(1L, i, 7));
        }
        assertThat(dashboard.getLatestReviews()).hasSize(6);
        assertThat(dashboard.getLatestReviews().getFirst().getReviewId()).isEqualTo(1L);

        dashboardService.handleDashboardEvent(reviewAddedEvent(1L, 99L, 9));

        assertThat(dashboard.getLatestReviews()).hasSize(6);
        assertThat(dashboard.getLatestReviews().getLast().getReviewId()).isEqualTo(99L);
        assertThat(dashboard.getLatestReviews().getFirst().getReviewId()).isEqualTo(2L);
    }

    @Test
    void addReview_saveIsCalled() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(reviewAddedEvent(1L, 10L, 8));

        verify(dashboardRepository).save(dashboard);
    }

    // --- removeReview ---

    @Test
    void removeReview_marksMatchingReviewAsDeleted() {
        var dashboard = buildDashboard(1L);
        dashboard.getStats().setReviewCount(1);
        dashboard.getStats().setRatingSum(8.0);
        var item = new DashboardReviewItem(10L, 5L, "Game", 8, "...", LocalDate.now(), false);
        dashboard.getLatestReviews().add(item);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(reviewDeletedEvent(1L, 10L, 8));

        assertThat(item.getIsDeleted()).isTrue();
    }

    @Test
    void removeReview_decrementsReviewCount() {
        var dashboard = buildDashboard(1L);
        dashboard.getStats().setReviewCount(2);
        dashboard.getStats().setRatingSum(16.0);
        dashboard.getLatestReviews().add(new DashboardReviewItem(10L, 5L, "Game", 8, "...", LocalDate.now(), false));
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(reviewDeletedEvent(1L, 10L, 8));

        assertThat(dashboard.getStats().getReviewCount()).isEqualTo(1);
    }

    @Test
    void removeReview_updatesAverageRating() {
        var dashboard = buildDashboard(1L);
        dashboard.getStats().setReviewCount(2);
        dashboard.getStats().setRatingSum(8.0);
        dashboard.getLatestReviews().add(new DashboardReviewItem(10L, 5L, "Game", 3, "...", LocalDate.now(), false));
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(reviewDeletedEvent(1L, 10L, 3));

        assertThat(dashboard.getStats().getAverageRating()).isEqualTo(5.0);
    }

    @Test
    void removeReview_reviewNotInLatestList_stillDecrementsCount() {
        var dashboard = buildDashboard(1L);
        dashboard.getStats().setReviewCount(1);
        dashboard.getStats().setRatingSum(8.0);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(reviewDeletedEvent(1L, 999L, 8));

        assertThat(dashboard.getStats().getReviewCount()).isEqualTo(0);
    }

    @Test
    void removeReview_lastReview_reviewCountBecomesZero() {
        var dashboard = buildDashboard(1L);
        dashboard.getStats().setReviewCount(1);
        dashboard.getStats().setRatingSum(7.0);
        dashboard.getLatestReviews().add(new DashboardReviewItem(10L, 5L, "Game", 7, "...", LocalDate.now(), false));
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(reviewDeletedEvent(1L, 10L, 7));

        assertThat(dashboard.getStats().getReviewCount()).isZero();
        assertThat(dashboard.getStats().getAverageRating()).isZero();
    }

    @Test
    void removeReview_incrementsVersion() {
        var dashboard = buildDashboard(1L);
        dashboard.getStats().setReviewCount(1);
        dashboard.getStats().setRatingSum(8.0);
        dashboard.getLatestReviews().add(new DashboardReviewItem(10L, 5L, "Game", 8, "...", LocalDate.now(), false));
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(reviewDeletedEvent(1L, 10L, 8));

        assertThat(dashboard.getVersion()).isEqualTo(2L);
    }

    // --- helpers ---

    private IntegrationEvent<ReviewPayload> reviewAddedEvent(Long userId, Long reviewId, int rating) {
        var payload = ReviewPayload.builder()
                .reviewId(reviewId)
                .gameId(5L)
                .gameTitle("Game")
                .rating(rating)
                .reviewPreview("Great game")
                .createdAt(LocalDate.now())
                .build();
        return new IntegrationEvent<>(EventType.REVIEW_ADDED, "game-service", userId, LocalDateTime.now(), payload);
    }

    private IntegrationEvent<ReviewPayload> reviewDeletedEvent(Long userId, Long reviewId, int rating) {
        var payload = ReviewPayload.builder()
                .reviewId(reviewId)
                .gameId(5L)
                .gameTitle("Game")
                .rating(rating)
                .createdAt(LocalDate.now())
                .build();
        return new IntegrationEvent<>(EventType.REVIEW_DELETED, "game-service", userId, LocalDateTime.now(), payload);
    }
}