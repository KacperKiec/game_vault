package io.kk.gameservice.repository;

import io.kk.gameservice.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {
    Review save(Review review);
    Optional<Review> findById(Long reviewId);
    Optional<Review> findByGuidAndUserId(Long guid, Long userId);
    List<Review> findByGuid(Long gameId);
    void deleteById(Long id);
}
