package io.kk.userservice.service;

import io.kk.envelope.IntegrationEvent;
import io.kk.payload.UserRegisteredPayload;
import io.kk.type.EventType;
import io.kk.userservice.dto.UserRegisterDTO;
import io.kk.userservice.dto.UserResponseDTO;
import io.kk.userservice.dto.UserUpdateRequestDTO;
import io.kk.userservice.dto.UsernameDTO;
import io.kk.userservice.exception.UserAlreadyExistException;
import io.kk.userservice.exception.UserNotFoundException;
import io.kk.userservice.integration.RabbitService;
import io.kk.userservice.model.Role;
import io.kk.userservice.model.User;
import io.kk.userservice.repository.UserRepository;
import io.kk.userservice.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Service responsible for managing user accounts and profiles.
 * It provides core CRUD operations, registration logic, and utility methods
 * for retrieving user-related data for internal services.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitService rabbitService;

    /**
     * Registers a new user in the system.
     * Validates that the email is unique before persisting the user with an encoded password
     * and a default 'USER' role. Sending event to dashboard service.
     *
     * @param userRegisterDTO The registration data including username, email, and plain-text password.
     * @return true if the user was successfully created.
     * @throws UserAlreadyExistException if a user with the provided email already exists.
     */
    public Boolean addUser(UserRegisterDTO userRegisterDTO) {
        var existingUser = userRepository.findByEmail(userRegisterDTO.email());

        if (existingUser.isPresent()) throw new UserAlreadyExistException("User already exists");

        User user = new User();
        user.setUsername(userRegisterDTO.username());
        user.setEmail(userRegisterDTO.email());
        user.setPassword(passwordEncoder.encode(userRegisterDTO.password()));
        user.setRole(Role.USER);

        var saved = userRepository.save(user);

        UserRegisteredPayload payload = UserRegisteredPayload.builder()
                .username(saved.getUsername())
                .email(saved.getEmail())
                .build();

        IntegrationEvent<UserRegisteredPayload> event = new IntegrationEvent<>(EventType.USER_REGISTERED, "user-service", saved.getId(), Instant.now(), payload);

        rabbitService.sendDashboardEvent(event);

        return true;
    }

    /**
     * Updates an existing user's profile information.
     *
     * @param updateRequestDTO DTO containing updated username, email, and role.
     * @return A {@link UserResponseDTO} representing the updated user profile.
     * @throws UserNotFoundException if no user is found with the specified ID.
     */
    public UserResponseDTO updateUser(UserUpdateRequestDTO updateRequestDTO) {
        var existingUser = userRepository.findById(updateRequestDTO.id()).orElseThrow(
                () -> new UserNotFoundException("User with id " + updateRequestDTO.id() + " was not found")
        );

        existingUser.setUsername(updateRequestDTO.username());
        existingUser.setEmail(updateRequestDTO.email());
        existingUser.setRole(updateRequestDTO.role());
        var saved = userRepository.save(existingUser);

        return Mapper.toUserDTO(saved);
    }

    /**
     * Retrieves all registered users in the system.
     *
     * @return A list of {@link UserResponseDTO} objects.
     */
    public List<UserResponseDTO> getUsers() {
        var users = userRepository.findAll();
        return users.stream().map(Mapper::toUserDTO).toList();
    }

    /**
     * Deletes a user from the system by their ID.
     *
     * @param id The unique identifier of the user to delete.
     * @throws UserNotFoundException if the user does not exist.
     */
    public void deleteUser(Long id) {
        var existingUser = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User with id " + id + " was not found")
        );

        userRepository.delete(existingUser);
    }

    /**
     * Checks if a user exists in the database.
     *
     * @param userId The unique identifier to check.
     * @return true if the user exists, false otherwise.
     */
    public Boolean isUserExisting(Long userId) {
        var existingUser = userRepository.findById(userId);
        return existingUser.isPresent();
    }

    /**
     * Retrieves a list of usernames and their corresponding IDs for a given set of user IDs.
     * This is primarily used for resolving user IDs to displayable names in other microservices.
     *
     * @param ids A list of user IDs to resolve.
     * @return A list of {@link UsernameDTO} objects containing ID and username mappings.
     */
    public List<UsernameDTO> getUsernames(List<Long> ids) {
        return userRepository.findAllByIdIn(ids).stream()
                .map(u -> UsernameDTO.builder()
                        .userId(u.getId())
                        .username(u.getUsername())
                        .build()).toList();
    }
}