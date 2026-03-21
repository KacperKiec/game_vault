package io.kk.userservice.dto;

import io.kk.userservice.model.Role;

public record AuthResponseDTO(String token, Long userId, String username, Role role) {}
