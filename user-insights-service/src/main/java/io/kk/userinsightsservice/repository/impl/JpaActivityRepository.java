package io.kk.userinsightsservice.repository.impl;

import io.kk.userinsightsservice.model.postgres.Activity;
import io.kk.userinsightsservice.repository.ActivityRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaActivityRepository extends JpaRepository<Activity, Long>, ActivityRepository {
}
