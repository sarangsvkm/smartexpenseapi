package com.srg.smartexpenseapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.srg.smartexpenseapi.entity.Loan;
import com.srg.smartexpenseapi.repository.LoanRepository;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DebtStrategyService {

    @Autowired
    private LoanRepository loanRepository;

    public List<Loan> getAvalancheStrategy(Long userId) {
        List<Loan> loans = loanRepository.findByUserId(userId);
        return loans.stream()
                .sorted(Comparator.comparing(Loan::getInterestRate).reversed())
                .collect(Collectors.toList());
    }

    public List<Loan> getSnowballStrategy(Long userId) {
        List<Loan> loans = loanRepository.findByUserId(userId);
        return loans.stream()
                .sorted(Comparator.comparing(l -> l.getRemainingBalance() != null ? l.getRemainingBalance() : l.getPrincipalAmount()))
                .collect(Collectors.toList());
    }
}
