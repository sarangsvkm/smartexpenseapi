package com.srg.smartexpenseapi.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;

@Entity
@Table(name = "income")
@Data
@NoArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Income {
    @Builder
    public Income(Long id, Double amount, String source, LocalDate date, IncomeCategory category, 
                  Boolean isTaxable, Double purchasePrice, LocalDate purchaseDate, 
                  String assetType, User user) {
        this.id = id;
        this.amount = amount;
        this.source = source;
        this.date = date;
        this.category = category;
        this.isTaxable = isTaxable != null ? isTaxable : true;
        this.purchasePrice = purchasePrice;
        this.purchaseDate = purchaseDate;
        this.assetType = assetType;
        this.user = user;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, columnDefinition = "varchar(255) default 'OTHER_SOURCES'")
    @Enumerated(EnumType.STRING)
    private IncomeCategory category;

    private Boolean isTaxable = true;

    // For Capital Gains (ITR-2/3)
    private Double purchasePrice;
    private LocalDate purchaseDate;
    private String assetType; // STOCKS, MUTUAL_FUND, GOLD, REAL_ESTATE, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;
}
