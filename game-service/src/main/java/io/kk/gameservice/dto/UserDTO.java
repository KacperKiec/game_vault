package io.kk.gameservice.dto;

import lombok.Builder;

@Builder
public record UserDTO (
        Long userId,
        String username
) {
}
