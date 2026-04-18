package com.srg.smartexpenseapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.srg.smartexpenseapi.entity.Loan;
import com.srg.smartexpenseapi.entity.Expense;
import com.srg.smartexpenseapi.entity.Income;
import com.srg.smartexpenseapi.entity.IncomeCategory;
import com.srg.smartexpenseapi.entity.RepaymentType;
import com.srg.smartexpenseapi.repository.LoanRepository;
import com.srg.smartexpenseapi.repository.ExpenseRepository;
import com.srg.smartexpenseapi.repository.IncomeRepository;
import com.srg.smartexpenseapi.payload.response.LoanPayoffProjection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LoanAnalysisService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;
    public LoanPayoffProjection calculateFastPayoff(Long userId, Long loanId, Double manualSurplus) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to loan");
        }

        double[] surplusData = calculateMonthlySurplusData(userId);
        double surplus = manualSurplus != null ? manualSurplus : surplusData[0];
        
        LoanPayoffProjection projection = simulatePayoff(loan, surplus);
        projection.setMonthlyIncome(surplusData[1]);
        projection.setMonthlyExpenses(surplusData[2]);
        projection.setMonthlySurplus(surplus);
        
        return projection;
    }

    private double[] calculateMonthlySurplusData(Long userId) {
        List<Income> incomes = incomeRepository.findByUserId(userId);
        List<Expense> expenses = expenseRepository.findByUserId(userId);

        if (incomes.isEmpty() && expenses.isEmpty()) {
            return new double[] { 0, 0, 0 };
        }

        double totalSalary = incomes.stream()
                .filter(i -> i.getCategory() == IncomeCategory.SALARY)
                .mapToDouble(Income::getAmount)
                .sum();
        
        double totalMonthlyExpenses = expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();
        
        // Calculate the number of months spanned by the data
        java.time.LocalDate minDate = java.time.LocalDate.now().minusMonths(1);
        java.time.LocalDate maxDate = java.time.LocalDate.now();

        for (Income i : incomes) {
            if (i.getDate().isBefore(minDate)) minDate = i.getDate();
            if (i.getDate().isAfter(maxDate)) maxDate = i.getDate();
        }
        for (Expense e : expenses) {
            if (e.getDate().isBefore(minDate)) minDate = e.getDate();
            if (e.getDate().isAfter(maxDate)) maxDate = e.getDate();
        }

        long months = java.time.temporal.ChronoUnit.MONTHS.between(
            minDate.withDayOfMonth(1), 
            maxDate.withDayOfMonth(1).plusMonths(1)
        );
        if (months <= 0) months = 1;

        double avgIncome = totalSalary / months;
        double avgExpense = totalMonthlyExpenses / months;
        double surplus = Math.max(0, avgIncome - avgExpense);

        return new double[] { surplus, avgIncome, avgExpense };
    }

    private LoanPayoffProjection simulatePayoff(Loan loan, double surplus) {
        double balance = loan.getRemainingBalance() != null ? loan.getRemainingBalance() : loan.getPrincipalAmount();
        double monthlyRate = (loan.getInterestRate() / 100.0) / 12.0;
        double regularEmi = loan.getEmiAmount() != null ? loan.getEmiAmount() : 0.0;
        RepaymentType strategy = loan.getRepaymentType() != null ? loan.getRepaymentType() : RepaymentType.EMI;
        
        // If EMI is not provided for standard loans, derive it
        if (regularEmi <= 0 && strategy == RepaymentType.EMI && loan.getTenureMonths() != null && loan.getTenureMonths() > 0) {
            regularEmi = (loan.getPrincipalAmount() * monthlyRate * Math.pow(1 + monthlyRate, loan.getTenureMonths())) 
                       / (Math.pow(1 + monthlyRate, loan.getTenureMonths()) - 1);
        } else if (regularEmi <= 0 && strategy == RepaymentType.MONTHLY_INTEREST) {
            regularEmi = loan.getPrincipalAmount() * monthlyRate;
        }

        int maxSimMonths = 600;
        
        // 1. Original Simulation (No Surplus)
        double originalInterest = 0;
        int originalMonths = 0;
        double tempBalance = balance;
        
        while (tempBalance > 0.01 && originalMonths < maxSimMonths) {
            originalMonths++;
            double interest = tempBalance * monthlyRate;
            originalInterest += interest;
            
            if (strategy == RepaymentType.EMI) {
                double principalPaid = Math.min(tempBalance, regularEmi - interest);
                tempBalance -= principalPaid;
            } else {
                // Monthly Interest or Bullet: Principal stays until tenure
                if (loan.getTenureMonths() != null && originalMonths >= loan.getTenureMonths()) {
                    tempBalance = 0;
                }
            }
            if (tempBalance <= 0) break;
        }

        // 2. Fast Payoff Simulation (With Surplus)
        double fastInterest = 0;
        int fastMonths = 0;
        tempBalance = balance;
        List<LoanPayoffProjection.MonthlyProjection> schedule = new ArrayList<>();
        double totalPaymentWithSurplus = regularEmi + surplus;

        while (tempBalance > 0.01 && fastMonths < maxSimMonths) {
            fastMonths++;
            double interest = tempBalance * monthlyRate;
            fastInterest += interest;
            
            // In all strategies, surplus reduces principal directly
            double principalPaid;
            if (strategy == RepaymentType.EMI) {
                principalPaid = totalPaymentWithSurplus - interest;
            } else {
                // For Bullet/IO, normally monthly base payment is 0 or interest-only.
                // We assume user pays (regularEmi + surplus) where regularEmi is base.
                principalPaid = totalPaymentWithSurplus - (strategy == RepaymentType.MONTHLY_INTEREST ? interest : 0);
            }

            if (principalPaid > tempBalance) principalPaid = tempBalance;
            if (principalPaid < 0) principalPaid = 0; // Should not happen with surplus
            
            tempBalance -= principalPaid;
            
            schedule.add(LoanPayoffProjection.MonthlyProjection.builder()
                .month(fastMonths)
                .payment(interest + principalPaid)
                .interest(interest)
                .principal(principalPaid)
                .remainingBalance(Math.max(0, tempBalance))
                .build());

            if (tempBalance <= 0) break;
            if (principalPaid <= 0 && fastMonths >= maxSimMonths) break;
        }

        return LoanPayoffProjection.builder()
            .loanId(loan.getId())
            .loanName(loan.getLoanName())
            .currentBalance(balance)
            .monthlySurplus(surplus)
            .totalInterestSaved(Math.max(0, originalInterest - fastInterest))
            .originalTenureMonths(originalMonths)
            .fastPayoffTenureMonths(fastMonths)
            .monthsSaved(Math.max(0, originalMonths - fastMonths))
            .projectionSchedule(schedule)
            .build();
    }
}
