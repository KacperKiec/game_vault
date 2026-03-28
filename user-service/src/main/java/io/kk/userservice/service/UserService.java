package io.kk.userservice.service;

import io.kk.userservice.dto.UserRegisterDTO;
import io.kk.userservice.dto.UserResponseDTO;
import io.kk.userservice.dto.UserUpdateRequestDTO;
import io.kk.userservice.dto.UsernameDTO;
import io.kk.userservice.exception.UserAlreadyExistException;
import io.kk.userservice.exception.UserNotFoundException;
import io.kk.userservice.model.Role;
import io.kk.userservice.model.User;
import io.kk.userservice.repository.UserRepository;
import io.kk.userservice.util.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Boolean addUser(UserRegisterDTO userRegisterDTO) {
        var existingUser = userRepository.findByEmail(userRegisterDTO.email());

        if (existingUser.isPresent()) throw new UserAlreadyExistException("User already exists");

        User user = new User();
        user.setUsername(userRegisterDTO.username());
        user.setEmail(userRegisterDTO.email());
        user.setPassword(passwordEncoder.encode(userRegisterDTO.password()));
        user.setRole(Role.USER);

        userRepository.save(user);

        return true;
    }

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

    public List<UserResponseDTO> getUsers() {
        var users = userRepository.findAll();
        return users.stream().map(Mapper::toUserDTO).toList();
    }

    public void deleteUser(Long id) {
        var existingUser = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User with id " + id + " was not found")
        );

        userRepository.delete(existingUser);
    }

    public Boolean isUserExisting(Long userId) {
        var  existingUser = userRepository.findById(userId);
        return existingUser.isPresent();
    }

    public List<UsernameDTO> getUsernames(List<Long> ids) {
        return userRepository.findAllByIdIn(ids).stream()
                .map(u -> {
                    return UsernameDTO.builder()
                            .userId(u.getId())
                            .username(u.getUsername())
                            .build();
                }).toList();
    }
}

