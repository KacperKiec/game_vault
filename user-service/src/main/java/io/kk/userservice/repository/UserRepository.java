package io.kk.userservice.repository;

import io.kk.userservice.model.Role;
import io.kk.userservice.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    List<User> findAll();
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
    void delete(User user);
}
