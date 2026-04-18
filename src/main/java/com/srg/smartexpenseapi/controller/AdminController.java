package com.srg.smartexpenseapi.controller;

import com.srg.smartexpenseapi.entity.User;
import com.srg.smartexpenseapi.payload.response.AdminUserResponse;
import com.srg.smartexpenseapi.repository.ExpenseRepository;
import com.srg.smartexpenseapi.repository.IncomeRepository;
import com.srg.smartexpenseapi.repository.LoanRepository;
import com.srg.smartexpenseapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private LoanRepository loanRepository;

    // ----------------------------------------------------------------
    // GET /api/admin/users
    // ----------------------------------------------------------------
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

    // ----------------------------------------------------------------
    // DELETE /api/admin/users/{id}
    // ----------------------------------------------------------------
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    // ----------------------------------------------------------------
    // GET /api/admin/users/{id}/expenses
    // ----------------------------------------------------------------
    @GetMapping("/users/{id}/expenses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserExpenses(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(expenseRepository.findByUserId(id));
    }

    // ----------------------------------------------------------------
    // GET /api/admin/users/{id}/income
    // ----------------------------------------------------------------
    @GetMapping("/users/{id}/income")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserIncome(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(incomeRepository.findByUserId(id));
    }

    // ----------------------------------------------------------------
    // GET /api/admin/users/{id}/loans
    // ----------------------------------------------------------------
    @GetMapping("/users/{id}/loans")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserLoans(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(loanRepository.findByUserId(id));
    }

    // ----------------------------------------------------------------
    // GET /api/admin/users/{id}/summary
    // ----------------------------------------------------------------
    @GetMapping("/users/{id}/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserSummary(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        double totalIncome = incomeRepository.findByUserId(id)
                .stream()
                .mapToDouble(i -> i.getAmount() != null ? i.getAmount() : 0.0)
                .sum();

        double totalExpenses = expenseRepository.findByUserId(id)
                .stream()
                .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0)
                .sum();

        double totalLoanBalance = loanRepository.findByUserId(id)
                .stream()
                .mapToDouble(l -> l.getRemainingBalance() != null ? l.getRemainingBalance() : 0.0)
                .sum();

        double netSavings = totalIncome - totalExpenses;

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpenses", totalExpenses);
        summary.put("totalLoanBalance", totalLoanBalance);
        summary.put("netSavings", netSavings);

        return ResponseEntity.ok(summary);
    }
}
