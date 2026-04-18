package com.srg.smartexpenseapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import com.srg.smartexpenseapi.entity.ERole;
import com.srg.smartexpenseapi.entity.Role;
import com.srg.smartexpenseapi.entity.DebtStrategy;
import com.srg.smartexpenseapi.entity.Category;
import com.srg.smartexpenseapi.repository.RoleRepository;
import com.srg.smartexpenseapi.repository.DebtStrategyRepository;
import com.srg.smartexpenseapi.repository.CategoryRepository;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.srg.smartexpenseapi.entity.User;
import com.srg.smartexpenseapi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DebtStrategyRepository debtStrategyRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(ERole.ROLE_USER));
            roleRepository.save(new Role(ERole.ROLE_MODERATOR));
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
            System.out.println("Roles initialized.");
        }

        if (debtStrategyRepository.count() == 0) {
            debtStrategyRepository.save(DebtStrategy.builder()
                .name("AVALANCHE")
                .description("Focuses on paying off debts with the highest interest rates first.")
                .build());
            debtStrategyRepository.save(DebtStrategy.builder()
                .name("SNOWBALL")
                .description("Focuses on paying off debts with the smallest balances first.")
                .build());
            System.out.println("Debt strategies initialized.");
        }

        if (categoryRepository.count() == 0) {
            Arrays.asList("Housing", "Food", "Transport", "Utilities", "Entertainment", "Health", "Shopping", "Others")
                .forEach(name -> categoryRepository.save(new Category(null, name)));
            System.out.println("Categories initialized.");
        }

        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = new User(adminUsername, adminEmail, passwordEncoder.encode(adminPassword));
            Set<Role> roles = new HashSet<>();
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(adminRole);
            admin.setRoles(roles);
            userRepository.save(admin);
            System.out.println("Admin user initialized.");
        }
    }
}
