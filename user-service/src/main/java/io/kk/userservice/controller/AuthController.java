package io.kk.userservice.controller;

import io.kk.userservice.dto.AuthRequestDTO;
import io.kk.userservice.dto.AuthResponseDTO;
import io.kk.userservice.dto.UserRegisterDTO;
import io.kk.userservice.service.AuthService;
import io.kk.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Boolean> register(@RequestBody @Valid UserRegisterDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.addUser(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid AuthRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(authService.login(dto));
    }
}
