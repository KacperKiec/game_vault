package io.kk.gameservice.dto;

import lombok.Builder;

@Builder
public record ParamDTO(Long id, String name, String slug) {
}
