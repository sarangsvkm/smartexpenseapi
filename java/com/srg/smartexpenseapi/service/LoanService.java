package com.srg.smartexpenseapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.srg.smartexpenseapi.entity.Loan;
import com.srg.smartexpenseapi.entity.User;
import com.srg.smartexpenseapi.entity.RepaymentType;
import com.srg.smartexpenseapi.repository.LoanRepository;
import java.util.List;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    public List<Loan> getLoansByUser(User user) {
        return loanRepository.findByUser(user);
    }

    public List<Loan> getLoansByUserId(Long userId) {
        return loanRepository.findByUserId(userId);
    }

    public Loan saveLoan(Loan loan) {
        if (loan.getRemainingBalance() == null) {
            loan.setRemainingBalance(loan.getPrincipalAmount());
        }

        // Auto-derive interest rate if missing but EMI/Total is present
        if ((loan.getInterestRate() == null || loan.getInterestRate() <= 0) 
            && loan.getEmiAmount() != null && loan.getEmiAmount() > 0) {
            
            if (loan.getRepaymentType() == RepaymentType.MONTHLY_INTEREST) {
                // Monthly Interest rate: R = (EMI / P) * 12 * 100
                double rate = (loan.getEmiAmount() / loan.getPrincipalAmount()) * 12 * 100;
                loan.setInterestRate((double) Math.round(rate * 100) / 100);
            } else if (loan.getRepaymentType() == RepaymentType.BULLET && loan.getTenureMonths() != null && loan.getTenureMonths() > 0) {
                // Bullet Simple Interest: Total = P * (1 + (r * n/12))
                // r = ((Total / P) - 1) / (n / 12)
                double total = loan.getEmiAmount(); // Use emiAmount field to store maturity total for Bullet
                double r = ((total / loan.getPrincipalAmount()) - 1) / (loan.getTenureMonths() / 12.0);
                loan.setInterestRate((double) Math.round(r * 100 * 100) / 100);
            } else if (loan.getTenureMonths() != null && loan.getTenureMonths() > 0) {
                // Amortizing rate using Newton-Raphson
                double derivedRate = deriveInterestRate(loan.getPrincipalAmount(), loan.getEmiAmount(), loan.getTenureMonths());
                loan.setInterestRate((double) Math.round(derivedRate * 100) / 100);
            }
        }

        // If interest rate is provided but EMI is not, calculate EMI/Total
        if ((loan.getEmiAmount() == null || loan.getEmiAmount() <= 0) 
            && loan.getPrincipalAmount() != null && loan.getInterestRate() != null) {
            
            if (loan.getRepaymentType() == RepaymentType.MONTHLY_INTEREST) {
                // Monthly Interest: EMI = P * (r / 12 / 100)
                double emi = loan.getPrincipalAmount() * (loan.getInterestRate() / 12 / 100);
                loan.setEmiAmount((double) Math.round(emi * 100) / 100);
            } else if (loan.getRepaymentType() == RepaymentType.BULLET && loan.getTenureMonths() != null) {
                // Bullet maturity: Total = P * (1 + (r * n / 12 / 100))
                double total = loan.getPrincipalAmount() * (1 + (loan.getInterestRate() * loan.getTenureMonths() / 12 / 100));
                loan.setEmiAmount((double) Math.round(total * 100) / 100);
            } else if (loan.getTenureMonths() != null && loan.getTenureMonths() > 0) {
                // Standard Amortizing EMI
                double p = loan.getPrincipalAmount();
                double r = loan.getInterestRate() / 12 / 100;
                int n = loan.getTenureMonths();
                if (r > 0) {
                    double emi = (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
                    loan.setEmiAmount((double) Math.round(emi * 100) / 100);
                } else {
                    loan.setEmiAmount((double) Math.round((p / n) * 100) / 100);
                }
            }
        }
        return loanRepository.save(loan);
    }

    /**
     * Derives annual interest rate from principal, monthly payment, and tenure using Newton-Raphson method.
     */
    private double deriveInterestRate(double p, double emi, int n) {
        double r = 0.01; // Initial guess: 1% monthly
        double epsilon = 0.0000001;
        int maxIterations = 100;

        for (int i = 0; i < maxIterations; i++) {
            double rn = Math.pow(1 + r, n);
            double f = p * r * rn - emi * (rn - 1);
            
            // Derivative of f(r) = P * ((1+r)^n + n*r*(1+r)^(n-1)) - E * n * (1+r)^(n-1)
            double df = p * (rn + n * r * Math.pow(1 + r, n - 1)) - emi * n * Math.pow(1 + r, n - 1);
            
            double rNext = r - f / df;
            if (Math.abs(rNext - r) < epsilon) {
                return rNext * 12 * 100; // Found it! Return annual %
            }
            r = rNext;
        }
        return r * 12 * 100; // Return last best guess
    }

    public void deleteLoan(Long id) {
        loanRepository.deleteById(id);
    }

    public Loan getLoanById(Long id) {
        return loanRepository.findById(id).orElse(null);
    }
}
