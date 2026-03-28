package io.kk.gameservice.controller;

import io.kk.gameservice.dto.ReviewRequestDTO;
import io.kk.gameservice.dto.ReviewResponseDTO;
import io.kk.gameservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/add")
    public ResponseEntity<ReviewResponseDTO> addReview(
            @RequestBody @Valid ReviewRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.parseLong(jwt.getSubject());
        var result = reviewService.addReview(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.parseLong(jwt.getSubject());
        reviewService.deleteReview(id, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/game/{guid}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviews(@PathVariable Long guid) {
        var result =  reviewService.getReviews(guid);
        return ResponseEntity.ok().body(result);
    }
}
