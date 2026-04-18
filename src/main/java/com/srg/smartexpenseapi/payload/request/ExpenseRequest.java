package com.srg.smartexpenseapi.payload.request;

import com.srg.smartexpenseapi.entity.Category;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ExpenseRequest {
    private Double amount;
    private String description;
    private LocalDate date;
    private Boolean isTaxDeductible;
    private String categoryName;
    private Category category;
}
