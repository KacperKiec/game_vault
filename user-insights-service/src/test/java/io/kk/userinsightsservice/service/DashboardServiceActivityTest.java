package io.kk.userinsightsservice.service;

import io.kk.userinsightsservice.dto.ActivityDTO;
import io.kk.userinsightsservice.model.postgres.ActivityType;
import io.kk.userinsightsservice.repository.DashboardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.kk.userinsightsservice.service.DashboardServiceCoreTest.buildDashboard;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceActivityTest {

    @Mock
    private DashboardRepository dashboardRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void updateRecentActivity_addsItemToRecentActivity() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.updateRecentActivity(gameAddedDto(1L, 42L, "Witcher", "WISHLIST"));

        assertThat(dashboard.getRecentActivity()).hasSize(1);
    }

    @Test
    void updateRecentActivity_setsActivityType() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.updateRecentActivity(gameAddedDto(1L, 42L, "Witcher", "WISHLIST"));

        assertThat(dashboard.getRecentActivity().getFirst().getType()).isEqualTo(ActivityType.GAME_ADDED_TO_LIST);
    }

    @Test
    void updateRecentActivity_setsGameIdAndTitle() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.updateRecentActivity(gameAddedDto(1L, 42L, "Witcher 3", "WISHLIST"));

        var item = dashboard.getRecentActivity().getFirst();
        assertThat(item.getGameId()).isEqualTo(42L);
        assertThat(item.getGameTitle()).isEqualTo("Witcher 3");
    }

    @Test
    void updateRecentActivity_setsOccurredAt() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));
        var now = LocalDateTime.of(2026, 4, 21, 12, 0);

        dashboardService.updateRecentActivity(new ActivityDTO(1L, ActivityType.GAME_ADDED_TO_LIST, now, 42L,
                Map.of("gameName", "Witcher", "newListTypeName", "WISHLIST")));

        assertThat(dashboard.getRecentActivity().getFirst().getOccurredAt()).isEqualTo(now);
    }

    @Test
    void updateRecentActivity_storesFullMetadataInDetails() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.updateRecentActivity(gameMovedDto(1L, 42L, "Witcher", "WISHLIST", "COMPLETED"));

        var details = dashboard.getRecentActivity().getFirst().getDetails();
        assertThat(details).containsKey("oldListType");
        assertThat(details).containsKey("newListType");
    }

    @Test
    void updateRecentActivity_reviewDeleted_nullGameTitle() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        var dto = new ActivityDTO(1L, ActivityType.REVIEW_DELETED, LocalDateTime.now(), 5L,
                Map.of("guid", 5L)); // no gameName for REVIEW_DELETED

        dashboardService.updateRecentActivity(dto);

        assertThat(dashboard.getRecentActivity().getFirst().getGameTitle()).isNull();
    }

    @Test
    void updateRecentActivity_whenListAtCapacity_removesOldestAndAddsNew() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        for (int i = 1; i <= 10; i++) {
            dashboardService.updateRecentActivity(gameAddedDto(1L, (long) i, "Game " + i, "WISHLIST"));
        }
        assertThat(dashboard.getRecentActivity()).hasSize(10);
        assertThat(dashboard.getRecentActivity().getFirst().getGameId()).isEqualTo(1L);

        dashboardService.updateRecentActivity(gameAddedDto(1L, 99L, "New Game", "COMPLETED"));

        assertThat(dashboard.getRecentActivity()).hasSize(10);
        assertThat(dashboard.getRecentActivity().getLast().getGameId()).isEqualTo(99L);
        assertThat(dashboard.getRecentActivity().getFirst().getGameId()).isEqualTo(2L);
    }

    @Test
    void updateRecentActivity_incrementsVersion() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.updateRecentActivity(gameAddedDto(1L, 42L, "Witcher", "WISHLIST"));

        assertThat(dashboard.getVersion()).isEqualTo(2L);
        verify(dashboardRepository).save(dashboard);
    }

    // --- helpers ---

    private ActivityDTO gameAddedDto(Long userId, Long gameId, String gameName, String listType) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("gameName", gameName);
        metadata.put("newListTypeName", listType);
        return new ActivityDTO(userId, ActivityType.GAME_ADDED_TO_LIST, LocalDateTime.now(), gameId, metadata);
    }

    private ActivityDTO gameMovedDto(Long userId, Long gameId, String gameName, String fromList, String toList) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("gameName", gameName);
        metadata.put("oldListType", fromList);
        metadata.put("newListType", toList);
        return new ActivityDTO(userId, ActivityType.GAME_MOVED_BETWEEN_LISTS, LocalDateTime.now(), gameId, metadata);
    }
}