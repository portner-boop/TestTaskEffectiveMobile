package com.example.testtaskeffectivemobile.util;

import com.example.testtaskeffectivemobile.entity.Role;
import com.example.testtaskeffectivemobile.entity.User;
import com.example.testtaskeffectivemobile.repository.RoleRepository;
import com.example.testtaskeffectivemobile.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void initData() {
        log.info("Checking and initializing data...");
        if (userRepository.findByEmailIgnoreCase("admin@example.com").isEmpty()) {
            log.info("Admin user not found, creating...");

            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> {
                        Role role = Role.builder()
                                .name("ROLE_ADMIN")
                                .createdBy("SYSTEM")
                                .lastModifiedBy("SYSTEM")
                                .build();
                        return roleRepository.save(role);
                    });

            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("System")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .enabled(true)
                    .locked(false)
                    .credentialsExpired(false)
                    .build();

            User savedAdmin = userRepository.save(admin);
            savedAdmin.setRoles(List.of(adminRole));
            userRepository.save(savedAdmin);

            log.info("Created admin user: admin@example.com / admin123");
        } else {
            log.info("Admin user already exists");
        }

        log.info("Data initialization complete");
    }
}