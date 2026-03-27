package io.kk.gameservice.controller;

import io.kk.gameservice.dto.GameDTO;
import io.kk.gameservice.dto.GameDetailsDTO;
import io.kk.gameservice.dto.GameParamsDTO;
import io.kk.gameservice.service.GameAPIService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GameAPIController {

    private final GameAPIService gameAPIService;

    @GetMapping("/games")
    public List<GameDTO> getGames(
            @RequestParam(name = "page_number", defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "name_search", required = false) String nameSearch,
            @RequestParam(name = "game_genres", required = false) List<String> gameGenres,
            @RequestParam(name = "game_platforms", required = false) List<String> gamePlatforms,
            @RequestParam(name = "dates", required = false) String dates,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = jwt != null ? Long.parseLong(jwt.getSubject()) : null;

        return gameAPIService.getGames(pageNumber, nameSearch, userId, gameGenres, gamePlatforms, dates);
    }

    @GetMapping("/game/{guid}")
    public GameDetailsDTO getGame(@PathVariable Long guid) {
        return gameAPIService.getGame(guid);
    }

    @GetMapping("/params")
    public GameParamsDTO getParams() {
        return gameAPIService.getParams();
    }
}
