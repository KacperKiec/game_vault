package io.kk.userinsightsservice.service;

import io.kk.userinsightsservice.exception.DashboardException;
import io.kk.userinsightsservice.model.mongo.DashboardDocument;
import io.kk.userinsightsservice.repository.DashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    /**
     * Metoda powinna przyjmować dto ze szczegółami zdarzenia i na tej podstawie wykonywać działanie,
     * np. dla inicjalizacji dashboard po rejestracji powinna tworzyć nowy dokument dla użytkownika,
     *     dla nowej recenzji dodawać recenzję, dla gry grę itd.
     */
    public void handleDashboardEvent() {

    }

    public DashboardDocument getUserDashboard(Long userId) {
        return dashboardRepository.findByUserId(userId).orElseThrow(
                () -> new DashboardException("Dashboard not found for user: " + userId)
        );
    }
}
