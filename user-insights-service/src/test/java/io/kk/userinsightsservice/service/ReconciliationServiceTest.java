package io.kk.userinsightsservice.service;

import io.kk.userinsightsservice.model.mongo.DashboardDocument;
import io.kk.userinsightsservice.repository.ActivityRepository;
import io.kk.userinsightsservice.repository.DashboardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReconciliationServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private DashboardRepository dashboardRepository;

    @Mock
    private RebuildService rebuildService;

    @InjectMocks
    private ReconciliationService reconciliationService;

    @Test
    void reconcile_noUsers_doesNotRebuild() {
        when(activityRepository.findDistinctUserIds()).thenReturn(List.of());

        reconciliationService.reconcile();

        verifyNoInteractions(rebuildService);
    }

    @Test
    void reconcile_versionMatchesActivityCount_doesNotRebuild() {
        when(activityRepository.findDistinctUserIds()).thenReturn(List.of(1L));
        when(activityRepository.countByUserId(1L)).thenReturn(3L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(buildDashboard(1L, 3L)));

        reconciliationService.reconcile();

        verifyNoInteractions(rebuildService);
    }

    @Test
    void reconcile_dashboardVersionBehind_triggersRebuild() {
        when(activityRepository.findDistinctUserIds()).thenReturn(List.of(1L));
        when(activityRepository.countByUserId(1L)).thenReturn(3L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(buildDashboard(1L, 1L)));

        reconciliationService.reconcile();

        verify(rebuildService).rebuildDashboard(1L);
    }

    @Test
    void reconcile_dashboardMissing_triggersRebuild() {
        when(activityRepository.findDistinctUserIds()).thenReturn(List.of(1L));
        when(activityRepository.countByUserId(1L)).thenReturn(2L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.empty());

        reconciliationService.reconcile();

        verify(rebuildService).rebuildDashboard(1L);
    }

    @Test
    void reconcile_multipleUsers_rebuildsOnlyOutOfSyncOnes() {
        when(activityRepository.findDistinctUserIds()).thenReturn(List.of(1L, 2L, 3L));
        when(activityRepository.countByUserId(1L)).thenReturn(2L);
        when(activityRepository.countByUserId(2L)).thenReturn(4L);
        when(activityRepository.countByUserId(3L)).thenReturn(1L);
        when(dashboardRepository.findByUserId(1L)).thenReturn(Optional.of(buildDashboard(1L, 2L)));  // in sync
        when(dashboardRepository.findByUserId(2L)).thenReturn(Optional.of(buildDashboard(2L, 2L)));  // behind
        when(dashboardRepository.findByUserId(3L)).thenReturn(Optional.empty());                     // missing

        reconciliationService.reconcile();

        verify(rebuildService, never()).rebuildDashboard(1L);
        verify(rebuildService).rebuildDashboard(2L);
        verify(rebuildService).rebuildDashboard(3L);
    }

    // --- helpers ---

    private DashboardDocument buildDashboard(Long userId, Long version) {
        var dashboard = new DashboardDocument();
        dashboard.setUserId(userId);
        dashboard.setVersion(version);
        return dashboard;
    }
}