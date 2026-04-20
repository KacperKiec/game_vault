package io.kk.userinsightsservice.repository;

import io.kk.userinsightsservice.model.postgres.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ActivityRepository {
    Activity save(Activity activity);
    Page<Activity> findByUserId(Long userId, Pageable pageable);
}
