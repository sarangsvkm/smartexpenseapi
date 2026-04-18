package com.srg.smartexpenseapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.srg.smartexpenseapi.entity.Expense;
import com.srg.smartexpenseapi.entity.Loan;
import com.srg.smartexpenseapi.repository.ExpenseRepository;
import com.srg.smartexpenseapi.repository.FinancialSnapshotRepository;
import com.srg.smartexpenseapi.repository.IncomeRepository;
import com.srg.smartexpenseapi.repository.LoanRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private FinancialSnapshotRepository financialSnapshotRepository;

    public Map<String, Object> getUserFinancialSummary(Long userId) {
        List<Expense> expenses = expenseRepository.findByUserId(userId);
        List<Loan> loans = loanRepository.findByUserId(userId);
        List<com.srg.smartexpenseapi.entity.Income> incomes = incomeRepository.findByUserId(userId);

        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double totalDebt = loans.stream().mapToDouble(l -> l.getRemainingBalance() != null ? l.getRemainingBalance() : 0.0).sum();
        double totalIncome = incomes.stream().mapToDouble(com.srg.smartexpenseapi.entity.Income::getAmount).sum();

        double savings = totalIncome - totalExpenses;
        double savingsPercentage = totalIncome > 0 ? (savings / totalIncome) * 100 : 0.0;

        Map<String, Double> expensesByCategory = expenses.stream()
                .filter(e -> e.getCategory() != null && e.getCategory().getName() != null)
                .collect(Collectors.groupingBy(e -> e.getCategory().getName(), 
                                             Collectors.summingDouble(Expense::getAmount)));

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalExpenses", totalExpenses);
        summary.put("totalIncome", totalIncome);
        summary.put("totalDebt", totalDebt);
        summary.put("savings", savings);
        summary.put("savingsPercentage", savingsPercentage);
        summary.put("expensesByCategory", expensesByCategory);
        summary.put("expenseCount", expenses.size());
        summary.put("loanCount", loans.size());

        String healthStatus = "Good";
        if (totalDebt > 1000000) healthStatus = "Critical";
        else if (totalDebt > 500000) healthStatus = "Warning";
        
        summary.put("healthStatus", healthStatus);

        return summary;
    }

    @Transactional
    public com.srg.smartexpenseapi.entity.FinancialSnapshot saveMonthlySnapshot(Long userId, Integer month, Integer year) {
        Map<String, Object> summary = getUserFinancialSummary(userId);
        com.srg.smartexpenseapi.entity.User user = new com.srg.smartexpenseapi.entity.User();
        user.setId(userId);

        com.srg.smartexpenseapi.entity.FinancialSnapshot snapshot = com.srg.smartexpenseapi.entity.FinancialSnapshot.builder()
                .user(user)
                .totalIncome((Double) summary.get("totalIncome"))
                .totalExpenses((Double) summary.get("totalExpenses"))
                .totalDebt((Double) summary.get("totalDebt"))
                .savingsPercentage((Double) summary.get("savingsPercentage"))
                .month(month)
                .year(year)
                .createdAt(java.time.LocalDateTime.now())
                .build();

        return financialSnapshotRepository.save(snapshot);
    }
}
