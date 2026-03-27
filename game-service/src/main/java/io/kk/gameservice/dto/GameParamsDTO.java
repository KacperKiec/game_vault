package io.kk.gameservice.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record GameParamsDTO (List<ParamDTO> genres, List<ParamDTO> platforms)
{ }
