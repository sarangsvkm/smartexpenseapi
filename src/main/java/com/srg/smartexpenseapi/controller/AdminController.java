package com.srg.smartexpenseapi.controller;

import com.srg.smartexpenseapi.entity.User;
import com.srg.smartexpenseapi.payload.response.AdminUserResponse;
import com.srg.smartexpenseapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    /**
     * GET /api/admin/users
     * Returns a list of all registered users (id, username, email, roles).
     * Restricted to users with ROLE_ADMIN.
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        List<User> users = userRepository.findAllWithRoles();

        List<AdminUserResponse> response = users.stream()
                .map(user -> {
                    List<String> roles = user.getRoles().stream()
                            .map(role -> role.getName().name())
                            .collect(Collectors.toList());
                    return new AdminUserResponse(
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            roles
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/admin/users/{id}
     * Deletes a user by ID.
     * Restricted to users with ROLE_ADMIN.
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok().body(java.util.Map.of("message", "User deleted successfully"));
    }
}
