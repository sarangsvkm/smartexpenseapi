package com.srg.smartexpenseapi.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.srg.smartexpenseapi.entity.ERole;
import com.srg.smartexpenseapi.entity.Role;
import com.srg.smartexpenseapi.repository.RoleRepository;

@Configuration
public class RoleInitializer {

    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.count() == 0) {
                roleRepository.save(new Role(ERole.ROLE_USER));
                roleRepository.save(new Role(ERole.ROLE_MODERATOR));
                roleRepository.save(new Role(ERole.ROLE_ADMIN));
                System.out.println("Initialized default roles: ROLE_USER, ROLE_MODERATOR, ROLE_ADMIN");
            }
        };
    }
}
