package io.kk.gameservice.service;

import io.kk.gameservice.dto.ReviewRequestDTO;
import io.kk.gameservice.dto.ReviewResponseDTO;
import io.kk.gameservice.exception.ReviewNotFoundException;
import io.kk.gameservice.integration.InternalServiceClient;
import io.kk.gameservice.integration.RabbitService;
import io.kk.gameservice.model.GameList;
import io.kk.gameservice.model.Review;
import io.kk.gameservice.repository.GameListRepository;
import io.kk.gameservice.repository.ReviewRepository;
import io.kk.gameservice.util.NotificationCreator;
import io.kk.gameservice.util.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final GameListRepository gameListRepository;
    private final InternalServiceClient internalServiceClient;
    private final RabbitService  rabbitService;

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

        var userIds = gameListRepository.findByGameId(dto.guid()).stream().map(GameList::getUserId).toList();

        String title = "New review";
        String content = "Someone added new review to the game you're interested in.";
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("guid", dto.guid());

        for (var u : userIds) {
            if (!userId.equals(u)) {
                var notificationRequestDTO = NotificationCreator.createNotification(NotificationType.NEW_REVIEW, u, title, content, metadata);
                rabbitService.sendNotification(notificationRequestDTO);
            }
        }

        var user = internalServiceClient.getUsernames(List.of(userId)).getFirst();

        return ReviewResponseDTO.builder()
                .id(saved.getId())
                .username(user.username())
                .content(saved.getContent())
                .rating(saved.getRating())
                .date(saved.getDate())
                .build();
    }

    public List<ReviewResponseDTO> getReviews(Long gameId) {
         var reviews =  reviewRepository.findByGuid(gameId);
         var users = internalServiceClient.getUsernames(reviews.stream().map(Review::getUserId).toList());



         return reviews.stream()
                 .map(r -> ReviewResponseDTO.builder()
                         .id(r.getId())
                         .username(users.stream().filter(u -> u.userId().equals(r.getUserId())).findFirst().get().username())
                         .content(r.getContent())
                         .rating(r.getRating())
                         .date(r.getDate())
                         .build()).toList();
    }

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
