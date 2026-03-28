package io.kk.gameservice.repository.impl;

import io.kk.gameservice.model.Review;
import io.kk.gameservice.repository.ReviewRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaReviewRepository extends JpaRepository<Review, Long>, ReviewRepository {

}
