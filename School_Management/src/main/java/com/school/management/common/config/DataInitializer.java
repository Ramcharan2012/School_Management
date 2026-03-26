package com.school.management.common.config;

import com.school.management.common.enums.Gender;
import com.school.management.common.enums.Role;
import com.school.management.common.enums.UserStatus;
import com.school.management.user.entity.User;
import com.school.management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DataInitializer - Seeds the first Admin user on application startup
 * if no admin exists in the database yet.
 *
 * This is a ONE-TIME setup. Once admin exists, this is skipped every time.
 *
 * Default Admin Credentials:
 * Email : admin@school.com
 * Password : Admin@123
 * Username : admin
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdminUser();
    }

    private void seedAdminUser() {
        // Check if any admin already exists
        if (userRepository.findByEmail("admin@school.com").isPresent()) {
            log.info("✅ Admin user already exists. Skipping seed.");
            return;
        }

        User admin = User.builder()
                .firstName("School")
                .lastName("Admin")
                .username("admin")
                .email("admin@school.com")
                .password(passwordEncoder.encode("Admin@123"))
                .phoneNumber("9999999999")
                .gender(Gender.MALE)
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .isFirstLogin(false)
                .build();

        userRepository.save(admin);

        log.info("========================================================");
        log.info("🎓 ADMIN USER SEEDED SUCCESSFULLY!");
        log.info("   Email    : admin@school.com");
        log.info("   Password : Admin@123");
        log.info("   Please change the password after first use.");
        log.info("========================================================");
    }
}
