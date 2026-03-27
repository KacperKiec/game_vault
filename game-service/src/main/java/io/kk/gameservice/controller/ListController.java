package io.kk.gameservice.controller;

import io.kk.gameservice.dto.GameListRequestDTO;
import io.kk.gameservice.dto.GameListResponseDTO;
import io.kk.gameservice.service.UserGameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/list")
@RequiredArgsConstructor
public class ListController {

    private final UserGameService userGameService;

    @PostMapping
    public ResponseEntity<Void> changeList(
            @Valid @RequestBody GameListRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = Long.parseLong(jwt.getSubject());

        userGameService.modifyLists(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<GameListResponseDTO> getUserLists(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.parseLong(jwt.getSubject());

        var result = userGameService.getLists(userId);
        return ResponseEntity.ok(result);
    }
}
