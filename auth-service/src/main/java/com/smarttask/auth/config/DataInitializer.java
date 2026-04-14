package com.smarttask.auth.config;

import com.smarttask.auth.entity.User;
import com.smarttask.auth.enums.Role;
import com.smarttask.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Profile("!prod")
    public CommandLineRunner seedUsers() {
        return args -> {
            if (userRepository.count() > 0) {
                log.info("Already seeded — skipping.");
                return;
            }

            userRepository.save(User.builder()
                    .username("admin")
                    .email("admin@smarttask.com")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("System Administrator")
                    .role(Role.ROLE_ADMIN)
                    .active(true).build());

            userRepository.save(User.builder()
                    .username("manager")
                    .email("manager@smarttask.com")
                    .password(passwordEncoder.encode("manager123"))
                    .fullName("Project Manager")
                    .role(Role.ROLE_MANAGER)
                    .active(true).build());

            userRepository.save(User.builder()
                    .username("alice")
                    .email("alice@smarttask.com")
                    .password(passwordEncoder.encode("alice123"))
                    .fullName("Alice Johnson")
                    .role(Role.ROLE_USER)
                    .active(true).build());

            log.info("Seeded 3 users: admin/admin123  manager/manager123  alice/alice123");
        };
    }
}
