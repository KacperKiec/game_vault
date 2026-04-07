package io.kk.gameservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    private Long id;
    private Long guid;
    private Long userId;
    private String content;
    private Integer rating;
    private LocalDate date;
}
