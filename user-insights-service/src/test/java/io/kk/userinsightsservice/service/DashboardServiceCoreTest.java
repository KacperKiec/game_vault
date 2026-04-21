package io.kk.userinsightsservice.service;

import io.kk.envelope.IntegrationEvent;
import io.kk.payload.UserRegisteredPayload;
import io.kk.type.EventType;
import io.kk.userinsightsservice.exception.DashboardException;
import io.kk.userinsightsservice.model.mongo.DashboardDocument;
import io.kk.userinsightsservice.model.mongo.DashboardListsPreview;
import io.kk.userinsightsservice.model.mongo.DashboardStats;
import io.kk.userinsightsservice.repository.DashboardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceCoreTest {

    @Mock
    private DashboardRepository dashboardRepository;

    @InjectMocks
    private DashboardService dashboardService;

    // --- getUserDashboard ---

    @Test
    void getUserDashboard_found_returnsDashboard() {
        var dashboard = new DashboardDocument();
        dashboard.setUserId(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        var result = dashboardService.getUserDashboard(1L);

        assertThat(result).isEqualTo(dashboard);
    }

    @Test
    void getUserDashboard_notFound_throwsDashboardException() {
        when(dashboardRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dashboardService.getUserDashboard(99L))
                .isInstanceOf(DashboardException.class)
                .hasMessageContaining("99");
    }

    // --- createDashboard ---

    @Test
    void createDashboard_setsUserIdVersionAndTimestamp() {
        var payload = UserRegisteredPayload.builder().username("alice").email("alice@example.com").build();
        var event = new IntegrationEvent<>(EventType.USER_REGISTERED, "user-service", 1L, LocalDateTime.now(), payload);
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        dashboardService.handleDashboardEvent(event);

        verify(dashboardRepository).save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getVersion()).isEqualTo(1L);
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void createDashboard_setsUsernameAndEmail() {
        var payload = UserRegisteredPayload.builder().username("alice").email("alice@example.com").build();
        var event = new IntegrationEvent<>(EventType.USER_REGISTERED, "user-service", 1L, LocalDateTime.now(), payload);
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        dashboardService.handleDashboardEvent(event);

        verify(dashboardRepository).save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void createDashboard_initializesAllStatsToZero() {
        var payload = UserRegisteredPayload.builder().username("alice").email("alice@example.com").build();
        var event = new IntegrationEvent<>(EventType.USER_REGISTERED, "user-service", 1L, LocalDateTime.now(), payload);
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        dashboardService.handleDashboardEvent(event);

        verify(dashboardRepository).save(captor.capture());
        var stats = captor.getValue().getStats();
        assertThat(stats.getWishlistCount()).isZero();
        assertThat(stats.getPlayingCount()).isZero();
        assertThat(stats.getCompletedCount()).isZero();
        assertThat(stats.getReviewCount()).isZero();
        assertThat(stats.getAverageRating()).isZero();
    }

    @Test
    void createDashboard_initializesAllListsAsEmpty() {
        var payload = UserRegisteredPayload.builder().username("alice").email("alice@example.com").build();
        var event = new IntegrationEvent<>(EventType.USER_REGISTERED, "user-service", 1L, LocalDateTime.now(), payload);
        var captor = ArgumentCaptor.forClass(DashboardDocument.class);

        dashboardService.handleDashboardEvent(event);

        verify(dashboardRepository).save(captor.capture());
        var saved = captor.getValue();
        assertThat(saved.getLatestReviews()).isEmpty();
        assertThat(saved.getRecentActivity()).isEmpty();
        assertThat(saved.getListsPreview().getWishlist()).isEmpty();
        assertThat(saved.getListsPreview().getPlaying()).isEmpty();
        assertThat(saved.getListsPreview().getCompleted()).isEmpty();
    }

    // --- helpers ---

    static DashboardDocument buildDashboard(Long userId) {
        var dashboard = new DashboardDocument();
        dashboard.setUserId(userId);
        dashboard.setVersion(1L);
        dashboard.setStats(new DashboardStats(0, 0, 0, 0, 0.0));
        dashboard.setListsPreview(new DashboardListsPreview(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        dashboard.setRecentActivity(new ArrayList<>());
        dashboard.setLatestReviews(new ArrayList<>());
        return dashboard;
    }
}