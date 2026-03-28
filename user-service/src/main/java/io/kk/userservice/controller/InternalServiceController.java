package io.kk.userservice.controller;

import io.kk.userservice.dto.UsernameDTO;
import io.kk.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/usernames")
    public ResponseEntity<List<UsernameDTO>> getUsernames(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(userService.getUsernames(ids));
    }

}
