package io.kk.gameservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class GameResponseDTO {
    private Long guid;
    private String name;
    private List<String> category;
    private String imageURL;
    private Date releaseDate;
    private String description;
}
