package com.srg.smartexpenseapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.srg.smartexpenseapi.entity.Income;
import com.srg.smartexpenseapi.entity.User;
import com.srg.smartexpenseapi.repository.UserRepository;
import com.srg.smartexpenseapi.security.services.UserDetailsImpl;
import com.srg.smartexpenseapi.service.IncomeService;
import java.util.List;

@RestController
@RequestMapping("/api/income")
@CrossOrigin(origins = "*", maxAge = 3600)
public class IncomeController {

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<Income> getIncome() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return incomeService.getIncomeByUserId(userDetails.getId());
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Income> addIncome(@RequestBody Income income) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        income.setUser(user);
        return ResponseEntity.ok(incomeService.saveIncome(income));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteIncome(@PathVariable Long id) {
        incomeService.deleteIncome(id);
        return ResponseEntity.ok().build();
    }
}
