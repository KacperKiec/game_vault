package io.kk.gameservice.service;

import io.kk.envelope.IntegrationEvent;
import io.kk.gameservice.dto.ActivityRequestDTO;
import io.kk.gameservice.dto.ReviewRequestDTO;
import io.kk.gameservice.dto.ReviewResponseDTO;
import io.kk.gameservice.exception.ReviewNotFoundException;
import io.kk.gameservice.integration.InternalServiceClient;
import io.kk.gameservice.integration.RabbitService;
import io.kk.gameservice.model.GameList;
import io.kk.gameservice.model.Review;
import io.kk.gameservice.repository.GameListRepository;
import io.kk.gameservice.repository.ReviewRepository;
import io.kk.gameservice.util.ActivityType;
import io.kk.gameservice.util.NotificationCreator;
import io.kk.gameservice.util.NotificationType;
import io.kk.payload.ReviewPayload;
import io.kk.type.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for managing game reviews.
 * It handles adding, retrieving, and deleting reviews, as well as triggering
 * notifications via RabbitMQ when new reviews are posted.
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final GameListRepository gameListRepository;
    private final InternalServiceClient internalServiceClient;
    private final RabbitService rabbitService;

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

        var userIds = gameListRepository.findByGameId(dto.guid()).stream()
                .map(GameList::getUserId)
                .toList();

        String title = "New review";
        String content = "Someone added new review to the game you're interested in.";
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("guid", dto.guid());

        for (var u : userIds) {
            if (!userId.equals(u)) {
                var notificationRequestDTO = NotificationCreator.createNotification(
                        NotificationType.NEW_REVIEW, u, title, content, metadata);
                rabbitService.sendNotification(notificationRequestDTO);
            }
        }

        sendActivity(userId, saved, dto.gameName(), ActivityType.REVIEW_ADDED);
        sendDashboardEvent(userId, saved, dto.gameName(), EventType.REVIEW_ADDED);

        var user = internalServiceClient.getUsernames(List.of(userId)).getFirst();

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
        var users = internalServiceClient.getUsernames(reviews.stream()
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

        sendActivity(userId, review, null, ActivityType.REVIEW_DELETED);
        sendDashboardEvent(userId, review, null, EventType.REVIEW_DELETED);
    }

    private void sendActivity(Long userId, Review review, String gameName, ActivityType activityType) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("guid", review.getGuid());
        if (gameName != null) metadata.put("gameName", gameName);

        ActivityRequestDTO activityRequestDTO = ActivityRequestDTO.builder()
                .userId(userId)
                .occurredAt(LocalDateTime.now())
                .relatedGameId(review.getGuid())
                .activityType(activityType)
                .metadata(metadata)
                .build();
        rabbitService.sendActivity(activityRequestDTO);
    }

    private void sendDashboardEvent(Long userId, Review review, String gameName, EventType eventType) {
        ReviewPayload payload = ReviewPayload.builder()
                .reviewId(review.getId())
                .rating(review.getRating())
                .reviewPreview(review.getContent())
                .gameTitle(gameName)
                .gameId(review.getGuid())
                .createdAt(review.getDate())
                .build();
        IntegrationEvent<ReviewPayload> event = new IntegrationEvent<>(eventType, "game-service", userId, LocalDateTime.now(), payload);
        rabbitService.sendDashboardEvent(event);
    }
}