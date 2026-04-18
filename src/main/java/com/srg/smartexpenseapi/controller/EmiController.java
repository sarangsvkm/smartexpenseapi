package com.srg.smartexpenseapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.srg.smartexpenseapi.entity.EmiPayment;
import com.srg.smartexpenseapi.entity.Loan;
import com.srg.smartexpenseapi.service.EmiPaymentService;
import com.srg.smartexpenseapi.service.LoanService;
import java.util.List;

@RestController
@RequestMapping("/api/emi")
@CrossOrigin(origins = "*", maxAge = 3600)
public class EmiController {

    @Autowired
    private EmiPaymentService emiPaymentService;

    @Autowired
    private LoanService loanService;

    @GetMapping("/loan/{loanId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<EmiPayment> getPayments(@PathVariable Long loanId) {
        return emiPaymentService.getPaymentsByLoan(loanId);
    }

    @PostMapping("/loan/{loanId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<EmiPayment> addPayment(@PathVariable Long loanId, @RequestBody EmiPayment payment) {
        Loan loan = loanService.getLoanById(loanId);
        if (loan == null) {
            return ResponseEntity.notFound().build();
        }
        payment.setLoan(loan);
        return ResponseEntity.ok(emiPaymentService.savePayment(payment));
    }
}
