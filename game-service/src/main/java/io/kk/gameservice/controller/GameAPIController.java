package io.kk.gameservice.controller;

import io.kk.gameservice.service.GameAPIService;
import io.kk.gameservice.util.Game;
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
    public List<Game> getGames(
            @RequestParam(name = "page_number", defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "name_search", required = false) String nameSearch,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.parseLong(jwt.getSubject());

        return gameAPIService.getGames(pageNumber, nameSearch, userId);
    }

    @GetMapping("/game/{guid}")
    public Game getGame(@PathVariable Long guid) {
        return gameAPIService.getGame(guid);
    }
}
