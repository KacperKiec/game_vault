package io.kk.userinsightsservice.service;

import io.kk.envelope.IntegrationEvent;
import io.kk.payload.GameMovedBetweenListsPayload;
import io.kk.payload.GameToListPayload;
import io.kk.type.EventType;
import io.kk.userinsightsservice.repository.DashboardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
    void gameAddedToList_addsItemToRecentActivity() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(gameAddedEvent(1L, 42L, "Witcher", "WISHLIST"));

        assertThat(dashboard.getRecentActivity()).hasSize(1);
    }

    @Test
    void gameAddedToList_setsActivityType() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(gameAddedEvent(1L, 42L, "Witcher", "WISHLIST"));

        assertThat(dashboard.getRecentActivity().getFirst().getType()).isEqualTo(EventType.GAME_ADDED_TO_LIST);
    }

    @Test
    void gameAddedToList_setsGameIdAndTitle() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(gameAddedEvent(1L, 42L, "Witcher 3", "WISHLIST"));

        var item = dashboard.getRecentActivity().getFirst();
        assertThat(item.getGameId()).isEqualTo(42L);
        assertThat(item.getGameTitle()).isEqualTo("Witcher 3");
    }

    @Test
    void gameAddedToList_setsOccurredAt() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));
        var now = LocalDateTime.of(2026, 4, 21, 12, 0);

        var payload = GameToListPayload.builder().gameId(42L).gameTitle("Witcher").listType("WISHLIST").build();
        dashboardService.handleDashboardEvent(new IntegrationEvent<>(EventType.GAME_ADDED_TO_LIST, "game-service", 1L, now, payload));

        assertThat(dashboard.getRecentActivity().getFirst().getOccurredAt()).isEqualTo(now);
    }

    @Test
    void gameMovedBetweenLists_storesFromListAndToListInDetails() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(gameMovedEvent(1L, 42L, "Witcher", "WISHLIST", "COMPLETED"));

        var details = dashboard.getRecentActivity().getFirst().getDetails();
        assertThat(details).containsKey("fromList");
        assertThat(details).containsKey("toList");
    }

    @Test
    void recentActivity_whenListAtCapacity_removesOldestAndAddsNew() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        for (int i = 1; i <= 10; i++) {
            dashboardService.handleDashboardEvent(gameAddedEvent(1L, (long) i, "Game " + i, "WISHLIST"));
        }
        assertThat(dashboard.getRecentActivity()).hasSize(10);
        assertThat(dashboard.getRecentActivity().getFirst().getGameId()).isEqualTo(1L);

        dashboardService.handleDashboardEvent(gameAddedEvent(1L, 99L, "New Game", "COMPLETED"));

        assertThat(dashboard.getRecentActivity()).hasSize(10);
        assertThat(dashboard.getRecentActivity().getLast().getGameId()).isEqualTo(99L);
        assertThat(dashboard.getRecentActivity().getFirst().getGameId()).isEqualTo(2L);
    }

    @Test
    void gameAddedToList_incrementsVersion() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(gameAddedEvent(1L, 42L, "Witcher", "WISHLIST"));

        assertThat(dashboard.getVersion()).isEqualTo(2L);
        verify(dashboardRepository).save(dashboard);
    }

    // --- helpers ---

    private IntegrationEvent<GameToListPayload> gameAddedEvent(Long userId, Long gameId, String gameName, String listType) {
        var payload = GameToListPayload.builder().gameId(gameId).gameTitle(gameName).listType(listType).build();
        return new IntegrationEvent<>(EventType.GAME_ADDED_TO_LIST, "game-service", userId, LocalDateTime.now(), payload);
    }

    private IntegrationEvent<GameMovedBetweenListsPayload> gameMovedEvent(Long userId, Long gameId, String gameName, String fromList, String toList) {
        var payload = GameMovedBetweenListsPayload.builder().gameId(gameId).gameTitle(gameName).fromList(fromList).toList(toList).build();
        return new IntegrationEvent<>(EventType.GAME_MOVED_BETWEEN_LISTS, "game-service", userId, LocalDateTime.now(), payload);
    }
}