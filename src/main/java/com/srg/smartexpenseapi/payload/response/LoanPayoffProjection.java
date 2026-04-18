package com.srg.smartexpenseapi.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanPayoffProjection {
    private Long loanId;
    private String loanName;
    private Double currentBalance;
    private Double monthlySurplus;
    private Double monthlyIncome;
    private Double monthlyExpenses;
    private Double totalInterestSaved;
    private Integer originalTenureMonths;
    private Integer fastPayoffTenureMonths;
    private Integer monthsSaved;
    private List<MonthlyProjection> projectionSchedule;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyProjection {
        private Integer month;
        private Double payment;
        private Double interest;
        private Double principal;
        private Double remainingBalance;
    }
}
