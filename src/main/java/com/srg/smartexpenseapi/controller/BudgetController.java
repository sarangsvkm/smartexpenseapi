package com.srg.smartexpenseapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.srg.smartexpenseapi.entity.Budget;
import com.srg.smartexpenseapi.entity.User;
import com.srg.smartexpenseapi.repository.UserRepository;
import com.srg.smartexpenseapi.security.services.UserDetailsImpl;
import com.srg.smartexpenseapi.service.BudgetService;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<Budget> getBudgets() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        return budgetService.getBudgetsByUser(user);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Budget> addBudget(@RequestBody Budget budget) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        budget.setUser(user);
        return ResponseEntity.ok(budgetService.saveBudget(budget));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.ok().build();
    }
}
