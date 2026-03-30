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

/**
 * Service responsible for user authentication and security operations.
 * It validates user credentials, handles password verification, and manages
 * the generation of JSON Web Tokens (JWT) for successful logins.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    /**
     * Authenticates a user based on their username and password.
     * If the credentials are valid, it generates a JWT containing user identity and roles.
     *
     * @param request The authentication request containing the username and plain-text password.
     * @return An {@link AuthResponseDTO} containing the generated JWT and basic user profile information.
     * @throws UserNotFoundException if the username does not exist or the password does not match.
     */
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

    /**
     * Compares a plain-text password with a hashed password stored in the database.
     *
     * @param plainPassword  The password provided by the user during login.
     * @param hashedPassword The encoded password retrieved from the user record.
     * @return true if the passwords match, false otherwise.
     */
    private boolean checkPassword(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }
}