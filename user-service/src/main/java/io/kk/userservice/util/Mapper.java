package io.kk.userservice.util;

import io.kk.userservice.dto.UserResponseDTO;
import io.kk.userservice.model.User;

public class Mapper {

    public static UserResponseDTO toUserDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
