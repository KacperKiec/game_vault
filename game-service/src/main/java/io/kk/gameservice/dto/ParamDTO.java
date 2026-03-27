package io.kk.gameservice.dto;

import lombok.Builder;

@Builder
public record ParamDTO(String name, String slug) {
}
