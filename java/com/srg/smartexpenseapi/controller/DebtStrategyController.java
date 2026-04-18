package com.srg.smartexpenseapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.srg.smartexpenseapi.entity.Loan;
import com.srg.smartexpenseapi.entity.User;
import com.srg.smartexpenseapi.entity.DebtStrategy;
import com.srg.smartexpenseapi.repository.UserRepository;
import com.srg.smartexpenseapi.repository.DebtStrategyRepository;
import com.srg.smartexpenseapi.security.services.UserDetailsImpl;
import com.srg.smartexpenseapi.service.DebtStrategyService;
import java.util.List;

@RestController
@RequestMapping("/api/strategies")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DebtStrategyController {

    @Autowired
    private DebtStrategyService debtStrategyService;

    @Autowired
    private DebtStrategyRepository debtStrategyRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/preference")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updatePreference(@RequestParam String strategy) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        
        DebtStrategy debtStrategy = debtStrategyRepository.findByName(strategy.toUpperCase())
            .orElseThrow(() -> new RuntimeException("Error: Strategy not found."));
            
        user.setPreferredStrategy(debtStrategy);
        userRepository.save(user);
        return ResponseEntity.ok(new com.srg.smartexpenseapi.payload.response.MessageResponse("Strategy preference updated to " + strategy));
    }

    @GetMapping("/recommended")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getRecommended() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        DebtStrategy preference = user.getPreferredStrategy();
        
        if (preference != null && "SNOWBALL".equalsIgnoreCase(preference.getName())) {
            return ResponseEntity.ok(debtStrategyService.getSnowballStrategy(userDetails.getId()));
        } else {
            return ResponseEntity.ok(debtStrategyService.getAvalancheStrategy(userDetails.getId()));
        }
    }

    @GetMapping("/avalanche")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getAvalanche() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(debtStrategyService.getAvalancheStrategy(userDetails.getId()));
    }

    @GetMapping("/snowball")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getSnowball() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(debtStrategyService.getSnowballStrategy(userDetails.getId()));
    }
}
