package io.kk.userservice.service;

import io.kk.userservice.model.Role;
import io.kk.userservice.model.User;
import io.kk.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service responsible for initializing the application data upon startup.
 * It implements {@link CommandLineRunner} to execute specific logic once the
 * Spring Boot application context is fully loaded.
 */
@Service
@RequiredArgsConstructor
public class StartupService implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Callback used to run the bean. It checks if an administrative account exists
     * in the database. If no admin is found, it creates a default one to ensure
     * the system is accessible after the first launch.
     *
     * @param args Incoming main method arguments.
     * @throws Exception if an error occurs during the startup logic.
     */
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