package io.kk.gameservice.dto;

import io.kk.gameservice.model.ListType;
import lombok.Builder;

import java.util.Date;
import java.util.List;

@Builder
public record GameDTO(
        Long guid,
        String name,
        List<String> genres,
        List<String> platforms,
        String backgroundImage,
        Date releaseDate,
        ListType listType
) {
}
