package com.srg.smartexpenseapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.srg.smartexpenseapi.entity.Income;
import com.srg.smartexpenseapi.entity.IncomeCategory;
import com.srg.smartexpenseapi.repository.IncomeRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaxRecommendationService {

    @Autowired
    private IncomeRepository incomeRepository;

    public String recommendItrForm(Long userId, Integer year) {
        List<Income> incomes = incomeRepository.findByUserId(userId).stream()
                .filter(i -> i.getDate().getYear() == year)
                .collect(Collectors.toList());

        if (incomes.isEmpty()) {
            return "No income data found for " + year;
        }

        Set<IncomeCategory> categories = incomes.stream()
                .map(Income::getCategory)
                .collect(Collectors.toSet());

        double totalIncome = incomes.stream().mapToDouble(Income::getAmount).sum();

        // Logic for ITR Recommendation (Simplified for Indian Tax context)
        
        // ITR-3: For business/professionals with full account audits
        if (categories.contains(IncomeCategory.BUSINESS_PROFESSION)) {
            return "ITR-3 (Appropriate for Business & Profession income)";
        }

        // ITR-4: Presumptive business (Sugam)
        if (categories.contains(IncomeCategory.PRESUMPTIVE_BUSINESS)) {
            return "ITR-4 (Sugam - Suitable for presumptive business/profession income)";
        }

        // ITR-2: For capital gains, foreigners, or income > 50L
        if (categories.contains(IncomeCategory.CAPITAL_GAINS) || totalIncome > 5000000) {
            return "ITR-2 (Required for Capital Gains or Income exceeding 50 Lakhs)";
        }

        // ITR-1: Salary + House Property + Other Sources (only one house property and income < 50L)
        return "ITR-1 (Sahaj - Easiest form for Salary and simple Income)";
    }
}
