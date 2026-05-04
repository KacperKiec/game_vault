package io.kk.userinsightsservice.service;

import io.kk.envelope.IntegrationEvent;
import io.kk.payload.GameMovedBetweenListsPayload;
import io.kk.payload.GameToListPayload;
import io.kk.type.EventType;
import io.kk.userinsightsservice.model.mongo.DashboardGamePreview;
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
class DashboardServiceGameTest {

    @Mock
    private DashboardRepository dashboardRepository;

    @InjectMocks
    private DashboardService dashboardService;

    // --- addGameToList ---

    @Test
    void addGameToList_wishlist_addsPreviewAndIncrementsCount() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(gameAddedEvent(1L, 42L, "Witcher", "WISHLIST"));

        assertThat(dashboard.getListsPreview().getWishlist()).hasSize(1);
        assertThat(dashboard.getListsPreview().getWishlist().getFirst().getGameId()).isEqualTo(42L);
        assertThat(dashboard.getStats().getWishlistCount()).isEqualTo(1);
    }

    @Test
    void addGameToList_owned_addsToPlayingAndIncrementsCount() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(gameAddedEvent(1L, 42L, "Witcher", "OWNED"));

        assertThat(dashboard.getListsPreview().getOwned()).hasSize(1);
        assertThat(dashboard.getStats().getPlayingCount()).isEqualTo(1);
        assertThat(dashboard.getListsPreview().getWishlist()).isEmpty();
        assertThat(dashboard.getListsPreview().getCompleted()).isEmpty();
    }

    @Test
    void addGameToList_completed_addsToCompletedAndIncrementsCount() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(gameAddedEvent(1L, 42L, "Witcher", "COMPLETED"));

        assertThat(dashboard.getListsPreview().getCompleted()).hasSize(1);
        assertThat(dashboard.getStats().getCompletedCount()).isEqualTo(1);
    }

    @Test
    void addGameToList_unknownListType_doesNotModifyAnyList() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(gameAddedEvent(1L, 42L, "Witcher", "NONE"));

        assertThat(dashboard.getListsPreview().getWishlist()).isEmpty();
        assertThat(dashboard.getListsPreview().getOwned()).isEmpty();
        assertThat(dashboard.getListsPreview().getCompleted()).isEmpty();
        assertThat(dashboard.getStats().getWishlistCount()).isZero();
    }

    @Test
    void addGameToList_setsGameTitle() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(gameAddedEvent(1L, 42L, "Witcher 3", "WISHLIST"));

        assertThat(dashboard.getListsPreview().getWishlist().getFirst().getTitle()).isEqualTo("Witcher 3");
    }

    @Test
    void addGameToList_incrementsVersion() {
        var dashboard = buildDashboard(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(gameAddedEvent(1L, 42L, "Witcher", "WISHLIST"));

        assertThat(dashboard.getVersion()).isEqualTo(2L);
        verify(dashboardRepository).save(dashboard);
    }

    // --- removeGameFromList ---

    @Test
    void removeGameFromList_wishlist_removesGameAndDecrementsCount() {
        var dashboard = buildDashboard(1L);
        dashboard.getStats().setWishlistCount(1);
        dashboard.getListsPreview().getWishlist().add(previewOf(42L));
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(gameRemovedEvent(1L, 42L, "WISHLIST"));

        assertThat(dashboard.getListsPreview().getWishlist()).isEmpty();
        assertThat(dashboard.getStats().getWishlistCount()).isZero();
    }

    @Test
    void removeGameFromList_owned_removesFromPlayingAndDecrementsCount() {
        var dashboard = buildDashboard(1L);
        dashboard.getStats().setPlayingCount(1);
        dashboard.getListsPreview().getOwned().add(previewOf(42L));
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(gameRemovedEvent(1L, 42L, "OWNED"));

        assertThat(dashboard.getListsPreview().getOwned()).isEmpty();
        assertThat(dashboard.getStats().getPlayingCount()).isZero();
    }

    @Test
    void removeGameFromList_completed_removesFromCompletedAndDecrementsCount() {
        var dashboard = buildDashboard(1L);
        dashboard.getStats().setCompletedCount(1);
        dashboard.getListsPreview().getCompleted().add(previewOf(42L));
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(gameRemovedEvent(1L, 42L, "COMPLETED"));

        assertThat(dashboard.getListsPreview().getCompleted()).isEmpty();
        assertThat(dashboard.getStats().getCompletedCount()).isZero();
    }

    @Test
    void removeGameFromList_onlyRemovesMatchingGame() {
        var dashboard = buildDashboard(1L);
        dashboard.getStats().setWishlistCount(2);
        dashboard.getListsPreview().getWishlist().add(previewOf(42L));
        dashboard.getListsPreview().getWishlist().add(previewOf(99L));
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(gameRemovedEvent(1L, 42L, "WISHLIST"));

        assertThat(dashboard.getListsPreview().getWishlist()).hasSize(1);
        assertThat(dashboard.getListsPreview().getWishlist().getFirst().getGameId()).isEqualTo(99L);
    }

    @Test
    void removeGameFromList_gameNotInList_countRemainsUnchanged() {
        var dashboard = buildDashboard(1L);
        dashboard.getStats().setWishlistCount(1);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(gameRemovedEvent(1L, 999L, "WISHLIST"));

        assertThat(dashboard.getListsPreview().getWishlist()).isEmpty();
        assertThat(dashboard.getStats().getWishlistCount()).isEqualTo(1);
    }

    // --- updateGameInList ---

    @Test
    void updateGameInList_movesGameBetweenLists() {
        var dashboard = buildDashboard(1L);
        dashboard.getStats().setWishlistCount(1);
        dashboard.getListsPreview().getWishlist().add(previewOf(42L));
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(gameMovedEvent(1L, 42L, "Witcher", "WISHLIST", "COMPLETED"));

        assertThat(dashboard.getListsPreview().getWishlist()).isEmpty();
        assertThat(dashboard.getStats().getWishlistCount()).isZero();
        assertThat(dashboard.getListsPreview().getCompleted()).hasSize(1);
        assertThat(dashboard.getStats().getCompletedCount()).isEqualTo(1);
    }

    @Test
    void updateGameInList_fromOwned_toWishlist() {
        var dashboard = buildDashboard(1L);
        dashboard.getStats().setPlayingCount(1);
        dashboard.getListsPreview().getOwned().add(previewOf(42L));
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(gameMovedEvent(1L, 42L, "Witcher", "OWNED", "WISHLIST"));

        assertThat(dashboard.getListsPreview().getOwned()).isEmpty();
        assertThat(dashboard.getStats().getPlayingCount()).isZero();
        assertThat(dashboard.getListsPreview().getWishlist()).hasSize(1);
        assertThat(dashboard.getStats().getWishlistCount()).isEqualTo(1);
    }

    @Test
    void updateGameInList_preservesGameTitleInNewList() {
        var dashboard = buildDashboard(1L);
        dashboard.getStats().setWishlistCount(1);
        dashboard.getListsPreview().getWishlist().add(previewOf(42L));
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(gameMovedEvent(1L, 42L, "Witcher 3", "WISHLIST", "COMPLETED"));

        assertThat(dashboard.getListsPreview().getCompleted().getFirst().getTitle()).isEqualTo("Witcher 3");
    }

    @Test
    void updateGameInList_doesNotAffectOtherGamesInSourceList() {
        var dashboard = buildDashboard(1L);
        dashboard.getStats().setWishlistCount(2);
        dashboard.getListsPreview().getWishlist().add(previewOf(42L));
        dashboard.getListsPreview().getWishlist().add(previewOf(77L));
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(dashboard));

        dashboardService.handleDashboardEvent(gameMovedEvent(1L, 42L, "Witcher", "WISHLIST", "COMPLETED"));

        assertThat(dashboard.getListsPreview().getWishlist()).hasSize(1);
        assertThat(dashboard.getListsPreview().getWishlist().getFirst().getGameId()).isEqualTo(77L);
        assertThat(dashboard.getStats().getWishlistCount()).isEqualTo(1);
    }

    // --- helpers ---

    private DashboardGamePreview previewOf(Long gameId) {
        var p = new DashboardGamePreview();
        p.setGameId(gameId);
        p.setTitle("Title");
        return p;
    }

    private IntegrationEvent<GameToListPayload> gameAddedEvent(Long userId, Long gameId, String title, String listType) {
        var payload = GameToListPayload.builder().gameId(gameId).gameTitle(title).listType(listType).build();
        return new IntegrationEvent<>(EventType.GAME_ADDED_TO_LIST, "game-service", userId, LocalDateTime.now(), payload);
    }

    private IntegrationEvent<GameToListPayload> gameRemovedEvent(Long userId, Long gameId, String listType) {
        var payload = GameToListPayload.builder().gameId(gameId).gameTitle("Title").listType(listType).build();
        return new IntegrationEvent<>(EventType.GAME_REMOVED_FROM_LIST, "game-service", userId, LocalDateTime.now(), payload);
    }

    private IntegrationEvent<GameMovedBetweenListsPayload> gameMovedEvent(Long userId, Long gameId, String title,
                                                                           String from, String to) {
        var payload = GameMovedBetweenListsPayload.builder().gameId(gameId).gameTitle(title).fromList(from).toList(to).build();
        return new IntegrationEvent<>(EventType.GAME_MOVED_BETWEEN_LISTS, "game-service", userId, LocalDateTime.now(), payload);
    }
}