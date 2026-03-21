package io.kk.userservice.service;

import io.kk.userservice.dto.AuthRequestDTO;
import io.kk.userservice.dto.AuthResponseDTO;
import io.kk.userservice.exception.UserNotFoundException;
import io.kk.userservice.model.User;
import io.kk.userservice.repository.UserRepository;
import io.kk.userservice.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    public AuthResponseDTO login(AuthRequestDTO request) {
        User user = userRepository.findByUsername(request.username()).orElseThrow(
                () -> new UserNotFoundException("User with username " + request.username() + " was not found")
        );

        boolean validPassword = checkPassword(request.password(), user.getPassword());

        if (!validPassword) {
            throw new UserNotFoundException("User not found");
        }

        String jwt = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        return new AuthResponseDTO(jwt, user.getId(), user.getUsername(), user.getRole());
    }

    private boolean checkPassword(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }
}
