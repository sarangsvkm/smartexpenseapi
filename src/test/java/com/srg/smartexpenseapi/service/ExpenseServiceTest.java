package com.srg.smartexpenseapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.srg.smartexpenseapi.entity.Category;
import com.srg.smartexpenseapi.entity.Expense;
import com.srg.smartexpenseapi.entity.User;
import com.srg.smartexpenseapi.payload.request.ExpenseRequest;
import com.srg.smartexpenseapi.payload.response.DiscoveryResponse;
import com.srg.smartexpenseapi.repository.CategoryRepository;
import com.srg.smartexpenseapi.repository.ExpenseRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SmartDiscoveryService discoveryService;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    void savesExpenseFromCategoryNamePayloadWithoutThrowing() {
        ExpenseRequest request = new ExpenseRequest();
        request.setAmount(5000.0);
        request.setDescription("monthly rent");
        request.setDate(LocalDate.of(2026, 4, 18));
        request.setCategoryName("housing_rent");
        request.setIsTaxDeductible(false);

        Category category = new Category("HOUSING_RENT");
        when(categoryRepository.findByNameIgnoreCase("HOUSING_RENT")).thenReturn(Optional.of(category));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Expense saved = expenseService.saveExpense(request, new User());

        assertEquals("HOUSING_RENT", saved.getCategory().getName());
        assertFalse(saved.getIsTaxDeductible());
        assertNotNull(saved.getUser());
    }

    @Test
    void fallsBackToDiscoveredCategoryWhenCategoryIsMissing() {
        ExpenseRequest request = new ExpenseRequest();
        request.setAmount(250.0);
        request.setDescription("zomato dinner");
        request.setDate(LocalDate.of(2026, 4, 18));

        when(discoveryService.discover("zomato dinner")).thenReturn(DiscoveryResponse.builder()
                .category("FOOD_DINING")
                .isTaxDeductible(false)
                .confidence("HIGH")
                .message("Discovered via Smart AI")
                .build());
        when(categoryRepository.findByNameIgnoreCase("FOOD_DINING")).thenReturn(Optional.of(new Category("FOOD_DINING")));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Expense saved = expenseService.saveExpense(request, new User());

        assertEquals("FOOD_DINING", saved.getCategory().getName());
    }
}
