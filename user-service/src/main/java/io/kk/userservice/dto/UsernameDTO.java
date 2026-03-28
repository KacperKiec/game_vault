package io.kk.userservice.dto;

import lombok.Builder;

@Builder
public record UsernameDTO(
        Long userId,
        String username
) {
}
