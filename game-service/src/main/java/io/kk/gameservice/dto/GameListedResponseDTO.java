package io.kk.gameservice.dto;

import io.kk.gameservice.model.ListType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameListedResponseDTO extends GameResponseDTO {

    private ListType listType;
}
