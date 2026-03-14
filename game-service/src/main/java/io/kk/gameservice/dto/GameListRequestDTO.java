package io.kk.gameservice.dto;

import io.kk.gameservice.model.ListType;
import jakarta.validation.constraints.NotNull;

public record GameListRequestDTO(
        @NotNull
        Long guid,

        @NotNull
        ListType listType
) { }