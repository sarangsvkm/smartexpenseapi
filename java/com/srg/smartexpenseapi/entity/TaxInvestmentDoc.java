package com.srg.smartexpenseapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tax_investment_docs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxInvestmentDoc {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String fileName;
    private String contentType;
    private String storagePath;
    
    // Category: e.g., "80C", "80D", "Insurance", "HLI (Home Loan Interest)"
    private String category;
    
    private Double amount;
    
    private Integer fiscalYear; // e.g., 2026

    private LocalDateTime uploadDate;

    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
    }
}
