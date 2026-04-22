package io.kk.userinsightsservice.repository;

import io.kk.userinsightsservice.model.postgres.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ActivityRepository {
    Activity save(Activity activity);
    Page<Activity> findByUserId(Long userId, Pageable pageable);
    Boolean existsByEventId(UUID eventId);
    List<Activity> findByUserIdOrderByOccurredAtAsc(Long userId);
    long countByUserId(Long userId);
    List<Long> findDistinctUserIds();
}
