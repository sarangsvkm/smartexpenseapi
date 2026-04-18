package com.srg.smartexpenseapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.srg.smartexpenseapi.entity.Income;
import com.srg.smartexpenseapi.entity.Expense;
import com.srg.smartexpenseapi.entity.IncomeCategory;
import com.srg.smartexpenseapi.payload.response.TaxReportResponse;
import com.srg.smartexpenseapi.repository.IncomeRepository;
import com.srg.smartexpenseapi.repository.ExpenseRepository;
import com.srg.smartexpenseapi.repository.TaxInvestmentDocRepository;
import com.srg.smartexpenseapi.entity.TaxInvestmentDoc;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaxCalculationService {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private TaxInvestmentDocRepository docRepository;

    @Autowired
    private MarketDataService marketDataService;

    public TaxReportResponse calculateTaxForYear(Long userId, Integer year, String itrType) {
        List<Income> incomes = incomeRepository.findByUserId(userId).stream()
                .filter(i -> i.getDate().getYear() == year)
                .collect(Collectors.toList());

        List<Expense> deductions = expenseRepository.findByUserId(userId).stream()
                .filter(e -> e.getDate().getYear() == year && Boolean.TRUE.equals(e.getIsTaxDeductible()))
                .collect(Collectors.toList());

        double salaryIncome = incomes.stream().filter(i -> i.getCategory() == IncomeCategory.SALARY).mapToDouble(Income::getAmount).sum();
        double businessIncome = incomes.stream().filter(i -> i.getCategory() == IncomeCategory.BUSINESS_PROFESSION).mapToDouble(Income::getAmount).sum();
        double presumptiveIncome = incomes.stream().filter(i -> i.getCategory() == IncomeCategory.PRESUMPTIVE_BUSINESS).mapToDouble(Income::getAmount).sum();
        double capitalGains = calculateCapitalGains(incomes);
        double otherIncome = incomes.stream().filter(i -> i.getCategory() == IncomeCategory.OTHER_SOURCES || i.getCategory() == IncomeCategory.HOUSE_PROPERTY).mapToDouble(Income::getAmount).sum();

        double totalExpenseDeductions = deductions.stream().mapToDouble(Expense::getAmount).sum();
        
        // Fetch verified deductions from uploaded documents
        List<TaxInvestmentDoc> verifiedDocs = docRepository.findByUserIdAndFiscalYear(userId, year);
        double verifiedDeductions = verifiedDocs.stream().mapToDouble(TaxInvestmentDoc::getAmount).sum();

        // Total deductions = Expenses marked deductible + Uploaded proofs
        double totalDeductions = totalExpenseDeductions + verifiedDeductions;
        
        // Calculate Live Portfolio Value
        double livePortfolioValue = incomes.stream()
                .filter(i -> i.getCategory() == IncomeCategory.CAPITAL_GAINS && i.getAssetType() != null)
                .mapToDouble(i -> marketDataService.estimateCurrentValue(i.getAssetType(), i.getAmount())) // Using sell amount as 'current' or 'basis' for simple demo
                .sum();

        // ITR-4 Presumptive Logic (6% if digital, but we'll assume 8% standard for simplicity in this model)
        double calculatedBusinessIncome = businessIncome + (presumptiveIncome * 0.08);

        double netTaxableIncome = (salaryIncome + calculatedBusinessIncome + capitalGains + otherIncome) - totalDeductions;
        if (netTaxableIncome < 0) netTaxableIncome = 0;

        boolean isSalaried = salaryIncome > 0;
        
        // Calculate both regimes
        double oldRegimeTax = calculateOldRegimeTax(netTaxableIncome, isSalaried);
        double newRegimeTax = calculateNewRegimeTax(netTaxableIncome, isSalaried);

        List<String> warnings = new java.util.ArrayList<>();
        if (itrType.equals("ITR-1")) {
            if (capitalGains > 0) warnings.add("ITR-1 is not applicable if you have Capital Gains. Please use ITR-2.");
            if (calculatedBusinessIncome > 0) warnings.add("ITR-1 is not applicable for Business/Professional income. Please use ITR-3/4.");
            if (salaryIncome + otherIncome > 5000000) warnings.add("ITR-1 is only for total income up to ₹50 Lakhs.");
        } else if (itrType.equals("ITR-4") && capitalGains > 0) {
            warnings.add("ITR-4 (Sugam) does not support Capital Gains. Please use ITR-3.");
        }

        double finalTaxBeforeCess = (newRegimeTax <= oldRegimeTax) ? newRegimeTax : oldRegimeTax;
        double cess = finalTaxBeforeCess * 0.04;

        Map<String, Double> cgBreakdown = incomes.stream()
                .filter(i -> i.getCategory() == IncomeCategory.CAPITAL_GAINS && i.getAssetType() != null)
                .collect(Collectors.groupingBy(
                        Income::getAssetType,
                        Collectors.summingDouble(i -> Math.max(0, i.getAmount() - (i.getPurchasePrice() != null ? i.getPurchasePrice() : 0)))
                ));

        String recommended = (newRegimeTax <= oldRegimeTax) ? "NEW_REGIME" : "OLD_REGIME";

        return TaxReportResponse.builder()
                .itrType(itrType)
                .totalSalaryIncome(salaryIncome)
                .totalBusinessIncome(calculatedBusinessIncome)
                .totalCapitalGains(capitalGains)
                .totalOtherIncome(otherIncome)
                .totalDeductions(totalDeductions)
                .verifiedDeductions(verifiedDeductions)
                .netTaxableIncome(netTaxableIncome)
                .livePortfolioValue(livePortfolioValue)
                .estimatedTax(finalTaxBeforeCess + cess) // Total Tax including Cess
                .estimatedTaxOldRegime(oldRegimeTax + (oldRegimeTax * 0.04))
                .estimatedTaxNewRegime(newRegimeTax + (newRegimeTax * 0.04))
                .recommendedRegime(recommended)
                .taxBracket(getTaxBracketName(netTaxableIncome, recommended))
                .incomeBreakdown(incomes.stream().collect(Collectors.groupingBy(i -> i.getCategory().name(), Collectors.summingDouble(Income::getAmount))))
                .capitalGainsBreakdown(cgBreakdown)
                .cessAmount(cess)
                .eligibilityWarnings(warnings)
                .build();
    }

    private double calculateOldRegimeTax(double income, boolean isSalaried) {
        // Standard Deduction: ₹50,000 for salaried
        double taxableIncome = isSalaried ? Math.max(0, income - 50000) : income;

        if (taxableIncome <= 250000) return 0;
        double tax = 0;
        
        // 5% slab (2.5L to 5L)
        if (taxableIncome > 250000) {
            tax += (Math.min(taxableIncome, 500000) - 250000) * 0.05;
        }
        // 20% slab (5L to 10L)
        if (taxableIncome > 500000) {
            tax += (Math.min(taxableIncome, 1000000) - 500000) * 0.20;
        }
        // 30% slab (> 10L)
        if (taxableIncome > 1000000) {
            tax += (taxableIncome - 1000000) * 0.30;
        }

        // Rebate 87A: Up to ₹5L in Old Regime
        if (taxableIncome <= 500000) return 0;

        return tax;
    }

    private double calculateNewRegimeTax(double income, boolean isSalaried) {
        // Standard Deduction: ₹75,000 for salaried (increased in Budget 2024)
        double taxableIncome = isSalaried ? Math.max(0, income - 75000) : income;

        // Rebate 87A: No tax if income is <= ₹12 Lakhs (FY 2024-25/25-26 update)
        if (taxableIncome <= 1200000) return 0;

        double tax = 0;
        // Slab 1: 0-4L (0%)
        // Slab 2: 4-8L (5%) -> 20,000
        if (taxableIncome > 400000) tax += (Math.min(taxableIncome, 800000) - 400000) * 0.05;
        // Slab 3: 8-12L (10%) -> 40,000
        if (taxableIncome > 800000) tax += (Math.min(taxableIncome, 1200000) - 800000) * 0.10;
        // Slab 4: 12-16L (15%) -> 60,000
        if (taxableIncome > 1200000) tax += (Math.min(taxableIncome, 1600000) - 1200000) * 0.15;
        // Slab 5: 16-20L (20%) -> 80,000
        if (taxableIncome > 1600000) tax += (Math.min(taxableIncome, 2000000) - 1600000) * 0.20;
        // Slab 6: 20-24L (25%) -> 100,000
        if (taxableIncome > 2000000) tax += (Math.min(taxableIncome, 2400000) - 2000000) * 0.25;
        // Slab 7: >24L (30%)
        if (taxableIncome > 2400000) tax += (taxableIncome - 2400000) * 0.30;

        return tax;
    }

    private String getTaxBracketName(double income, String regime) {
        if ("NEW_REGIME".equals(regime)) {
            if (income <= 400000) return "Exempt";
            if (income <= 1200000) return "Exempt (Rebate)";
            if (income <= 1600000) return "15%";
            if (income <= 2000000) return "20%";
            if (income <= 2400000) return "25%";
            return "30%";
        } else {
            if (income <= 250000) return "Exempt";
            if (income <= 500000) return "Exempt (Rebate)";
            if (income <= 1000000) return "20%";
            return "30%";
        }
    }
    private double calculateCapitalGains(List<Income> incomes) {
        return incomes.stream()
                .filter(i -> i.getCategory() == IncomeCategory.CAPITAL_GAINS)
                .mapToDouble(i -> {
                    double sellPrice = i.getAmount();
                    double buyPrice = i.getPurchasePrice() != null ? i.getPurchasePrice() : 0;
                    return Math.max(0, sellPrice - buyPrice);
                }).sum();
    }
}
