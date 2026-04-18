package com.srg.smartexpenseapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.srg.smartexpenseapi.entity.Loan;
import com.srg.smartexpenseapi.entity.User;
import com.srg.smartexpenseapi.repository.UserRepository;
import com.srg.smartexpenseapi.security.services.UserDetailsImpl;
import com.srg.smartexpenseapi.service.LoanService;
import com.srg.smartexpenseapi.service.LoanAnalysisService;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LoanController {

    @Autowired
    private LoanService loanService;

    @Autowired
    private LoanAnalysisService loanAnalysisService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<Loan> getAllLoans() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return loanService.getLoansByUserId(userDetails.getId());
    }

    @GetMapping("/{id}/fast-payoff")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<com.srg.smartexpenseapi.payload.response.LoanPayoffProjection> getFastPayoffProjection(
            @PathVariable Long id, 
            @RequestParam(required = false) Double manualSurplus) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(loanAnalysisService.calculateFastPayoff(userDetails.getId(), id, manualSurplus));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Loan> addLoan(@RequestBody Loan loan) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        loan.setUser(user);
        return ResponseEntity.ok(loanService.saveLoan(loan));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteLoan(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return ResponseEntity.ok().build();
    }
}
