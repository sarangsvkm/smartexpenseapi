package com.srg.smartexpenseapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.srg.smartexpenseapi.entity.EmiPayment;
import com.srg.smartexpenseapi.entity.Loan;
import com.srg.smartexpenseapi.repository.EmiPaymentRepository;
import com.srg.smartexpenseapi.repository.LoanRepository;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmiPaymentService {

    @Autowired
    private EmiPaymentRepository emiPaymentRepository;

    @Autowired
    private LoanRepository loanRepository;

    public List<EmiPayment> getPaymentsByLoan(Long loanId) {
        return emiPaymentRepository.findByLoanId(loanId);
    }

    @Transactional
    public EmiPayment savePayment(EmiPayment payment) {
        EmiPayment savedPayment = emiPaymentRepository.save(payment);
        
        // Update loan remaining balance
        Loan loan = payment.getLoan();
        if (loan != null && "Paid".equalsIgnoreCase(payment.getStatus())) {
            double currentBalance = loan.getRemainingBalance() != null ? loan.getRemainingBalance() : loan.getPrincipalAmount();
            loan.setRemainingBalance(currentBalance - payment.getAmount());
            loanRepository.save(loan);
        }
        
        return savedPayment;
    }
}
