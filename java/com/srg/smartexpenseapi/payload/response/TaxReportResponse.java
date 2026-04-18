package com.srg.smartexpenseapi.payload.response;

import lombok.Builder;
import lombok.Data;
import java.util.Map;
import java.util.List;

@Data
@Builder
public class TaxReportResponse {
    private String itrType;
    private Double totalSalaryIncome;
    private Double totalBusinessIncome;
    private Double totalCapitalGains;
    private Double totalOtherIncome;
    private Double totalDeductions;
    private Double verifiedDeductions; // From uploaded documents
    private Double netTaxableIncome;
    private Double livePortfolioValue; // Current market value of assets
    private Double estimatedTax; // This will show the recommended (lower) tax
    private Double estimatedTaxOldRegime;
    private Double estimatedTaxNewRegime;
    private String recommendedRegime;
    private String taxBracket;
    private Map<String, Double> incomeBreakdown;
    private Map<String, Double> capitalGainsBreakdown;
    private Double cessAmount;
    private List<String> eligibilityWarnings;
}
