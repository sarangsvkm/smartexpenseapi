package com.srg.smartexpenseapi.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Loan {
    @Builder
    public Loan(Long id, String loanName, String financeName, String purpose, Double principalAmount, 
            Double interestRate, Integer tenureMonths, LocalDate startDate, Double remainingBalance, 
            Double emiAmount, LoanType loanType, User user) {
        this.id = id;
        this.loanName = loanName;
        this.financeName = financeName;
        this.purpose = purpose;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.tenureMonths = tenureMonths;
        this.startDate = startDate;
        this.remainingBalance = remainingBalance;
        this.emiAmount = emiAmount;
        this.loanType = loanType != null ? loanType : LoanType.PERSONAL;
        this.user = user;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String loanName;

    private String financeName;

    private String purpose;


    @Column(nullable = false)
    private Double principalAmount;

    @Column(nullable = false)
    private Double interestRate;

    @Column(nullable = false)
    private Integer tenureMonths;

    @Column(nullable = false)
    private LocalDate startDate;

    private Double remainingBalance;

    private Double emiAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", columnDefinition = "varchar(255) default 'PERSONAL'")
    private LoanType loanType = LoanType.PERSONAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "repayment_type", columnDefinition = "varchar(255) default 'EMI'")
    private RepaymentType repaymentType = RepaymentType.EMI;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;
}
