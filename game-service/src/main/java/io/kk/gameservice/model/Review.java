package io.kk.gameservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id", unique = true, nullable = false)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private Long guid;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "rating", nullable = false)
    @Min(value = 1)
    @Max(value = 5)
    private Integer rating;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime date;

    @Column(name = "game_name", nullable = false)
    private String gameName;

}
