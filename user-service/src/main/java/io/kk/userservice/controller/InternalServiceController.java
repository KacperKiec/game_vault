package io.kk.userservice.controller;

import io.kk.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalServiceController {

    private final UserService userService;

    @GetMapping("/user/{id}")
    public ResponseEntity<Boolean> userExists(@PathVariable Long id) {
        var result = userService.isUserExisting(id);
        return ResponseEntity.ok(result);
    }
}
