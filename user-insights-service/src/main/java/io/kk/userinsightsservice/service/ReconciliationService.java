package io.kk.userinsightsservice.service;

import io.kk.userinsightsservice.repository.ActivityRepository;
import io.kk.userinsightsservice.repository.DashboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final ActivityRepository activityRepository;
    private final DashboardRepository dashboardRepository;
    private final RebuildService rebuildService;

    @Scheduled(
            initialDelayString = "${app.insights.reconciliation.initial-delay-ms:10000}",
            fixedDelayString   = "${app.insights.reconciliation.interval-ms:60000}")
    public void reconcile() {
        var userIds = activityRepository.findDistinctUserIds();
        for (Long userId : userIds) {
            long activityCount = activityRepository.countByUserId(userId);
            var dashboard = dashboardRepository.findByUserId(userId);

            if (dashboard.isEmpty() || dashboard.get().getVersion() != activityCount) {
                log.warn("Dashboard out of sync for userId={}: version={}, activityCount={}",
                        userId, dashboard.map(d -> d.getVersion().toString()).orElse("missing"), activityCount);

                rebuildService.rebuildDashboard(userId);
            }
        }
    }
}