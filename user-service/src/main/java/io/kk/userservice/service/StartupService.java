package io.kk.userservice.service;

import io.kk.userservice.model.Role;
import io.kk.userservice.model.User;
import io.kk.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StartupService implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.findByRole(Role.ADMIN).isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setRole(Role.ADMIN);
            adminUser.setPassword(passwordEncoder.encode("admin"));

            userRepository.save(adminUser);
            System.out.println("Default admin account was created.");
        }
    }
}
