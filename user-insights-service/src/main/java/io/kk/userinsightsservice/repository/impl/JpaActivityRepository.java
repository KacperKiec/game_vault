package io.kk.userinsightsservice.repository.impl;

import io.kk.userinsightsservice.model.postgres.Activity;
import io.kk.userinsightsservice.repository.ActivityRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaActivityRepository extends JpaRepository<Activity, Long>, ActivityRepository {

    @Query("SELECT DISTINCT a.userId FROM Activity a")
    List<Long> findDistinctUserIds();
}
