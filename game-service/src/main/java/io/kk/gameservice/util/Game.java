package io.kk.gameservice.util;

import io.kk.gameservice.model.ListType;
import lombok.Builder;

import java.util.Date;
import java.util.List;

@Builder
public record Game(
        Long guid,
        String name,
        List<String> genres,
        String backgroundImg,
        Date releaseDate,
        String description,
        ListType listType
) {
}
