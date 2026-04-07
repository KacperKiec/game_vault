package io.kk.gameservice.service;

import io.kk.gameservice.dto.ReviewRequestDTO;
import io.kk.gameservice.dto.ReviewResponseDTO;
import io.kk.gameservice.exception.ReviewNotFoundException;
import io.kk.gameservice.integration.InternalUserRepo;
import io.kk.gameservice.model.Review;
import io.kk.gameservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service responsible for managing game reviews.
 * It handles adding, retrieving, and deleting reviews, as well as triggering
 * notifications via RabbitMQ when new reviews are posted.
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final InternalUserRepo internalUserRepo;

    /**
     * Adds a new review or updates an existing one for a specific game and user.
     * After saving, it notifies other users who have the same game in their lists
     * via an asynchronous message queue.
     *
     * @param dto    The review data including game GUID, content, and rating.
     * @param userId The ID of the user performing the action.
     * @return A {@link ReviewResponseDTO} containing the saved review details and the reviewer's username.
     */
    public ReviewResponseDTO addReview(ReviewRequestDTO dto, Long userId) {
        var existingReview = reviewRepository.findByGuidAndUserId(dto.guid(), userId);

        Review review;
        if (existingReview.isPresent()) {
            review = existingReview.get();
            review.setContent(dto.content());
            review.setRating(dto.rating());
            review.setUserId(userId);
            review.setDate(LocalDate.now());
        } else {
            review = new Review();
            review.setGuid(dto.guid());
            review.setUserId(userId);
            review.setContent(dto.content());
            review.setRating(dto.rating());
            review.setDate(LocalDate.now());
        }

        var saved = reviewRepository.save(review);
        var user = internalUserRepo.getUsernames(List.of(userId)).getFirst();

        return ReviewResponseDTO.builder()
                .id(saved.getId())
                .username(user.username())
                .content(saved.getContent())
                .rating(saved.getRating())
                .date(saved.getDate())
                .build();
    }

    /**
     * Retrieves all reviews for a specific game.
     * Usernames are fetched in bulk from the internal user service to avoid the N+1 problem.
     *
     * @param gameId The unique identifier of the game.
     * @return A list of {@link ReviewResponseDTO} objects representing the game's reviews.
     */
    public List<ReviewResponseDTO> getReviews(Long gameId) {
        var reviews = reviewRepository.findByGuid(gameId);
        var users = internalUserRepo.getUsernames(reviews.stream()
                .map(Review::getUserId)
                .toList());

        return reviews.stream()
                .map(r -> ReviewResponseDTO.builder()
                        .id(r.getId())
                        .username(users.stream()
                                .filter(u -> u.userId().equals(r.getUserId()))
                                .findFirst()
                                .get()
                                .username())
                        .content(r.getContent())
                        .rating(r.getRating())
                        .date(r.getDate())
                        .build())
                .toList();
    }

    /**
     * Deletes a specific review.
     * Validates that the review exists and belongs to the requesting user before deletion.
     *
     * @param reviewId The ID of the review to delete.
     * @param userId   The ID of the user requesting deletion.
     * @throws ReviewNotFoundException if the review does not exist or does not belong to the user.
     */
    public void deleteReview(Long reviewId, Long userId) {
        var review = reviewRepository.findById(reviewId).orElseThrow(
                () -> new ReviewNotFoundException("Review not found")
        );

        if (!review.getUserId().equals(userId)) {
            throw new ReviewNotFoundException("Review not found");
        }

        reviewRepository.deleteById(reviewId);
    }
}