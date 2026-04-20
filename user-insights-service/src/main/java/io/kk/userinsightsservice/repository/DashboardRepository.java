package io.kk.userinsightsservice.repository;

import io.kk.userinsightsservice.model.mongo.DashboardDocument;

import java.util.Optional;

public interface DashboardRepository {
    DashboardDocument save(DashboardDocument dashboard);
    Optional<DashboardDocument> findByUserId(Long userId);
}
