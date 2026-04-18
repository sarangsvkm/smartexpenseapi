package com.srg.smartexpenseapi.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Expense {
    @Builder
    public Expense(Long id, Double amount, String description, LocalDate date, 
                   Boolean isTaxDeductible, User user, Category category) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.isTaxDeductible = isTaxDeductible != null ? isTaxDeductible : false;
        this.user = user;
        this.category = category;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double amount;

    private String description;

    @Column(nullable = false)
    private LocalDate date;

    private Boolean isTaxDeductible = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;
}
