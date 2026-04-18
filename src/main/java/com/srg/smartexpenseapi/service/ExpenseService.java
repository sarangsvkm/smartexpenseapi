package com.srg.smartexpenseapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.srg.smartexpenseapi.entity.Expense;
import com.srg.smartexpenseapi.entity.User;
import com.srg.smartexpenseapi.payload.request.ExpenseRequest;
import com.srg.smartexpenseapi.payload.response.DiscoveryResponse;
import com.srg.smartexpenseapi.entity.Category;
import com.srg.smartexpenseapi.repository.CategoryRepository;
import com.srg.smartexpenseapi.repository.ExpenseRepository;
import java.util.List;

@Service
@Transactional
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Expense> getAllExpensesByUser(User user) {
        return expenseRepository.findByUser(user);
    }

    public List<Expense> getAllExpensesByUserId(Long userId) {
        return expenseRepository.findByUserId(userId);
    }

    @Autowired
    private SmartDiscoveryService discoveryService;

    public Expense saveExpense(ExpenseRequest request, User user) {
        Expense expense = new Expense();
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setDate(request.getDate());
        expense.setIsTaxDeductible(request.getIsTaxDeductible() != null ? request.getIsTaxDeductible() : false);
        expense.setUser(user);
        expense.setCategory(resolveIncomingCategory(request));
        return saveExpense(expense);
    }

    public Expense saveExpense(Expense expense) {
        String requestedCategoryName = normalizeCategoryName(expense.getCategory() != null ? expense.getCategory().getName() : null);

        if (requestedCategoryName == null || "MISCELLANEOUS".equals(requestedCategoryName)) {
            DiscoveryResponse discovery = discoveryService.discover(expense.getDescription());

            String discoveredCategoryName = normalizeCategoryName(discovery.getCategory());
            Category category = getOrCreateCategory(discoveredCategoryName != null ? discoveredCategoryName : "MISCELLANEOUS");
            expense.setCategory(category);

            if (expense.getIsTaxDeductible() == null || !expense.getIsTaxDeductible()) {
                expense.setIsTaxDeductible(Boolean.TRUE.equals(discovery.getIsTaxDeductible()));
            }
        } else {
            Category category = getOrCreateCategory(requestedCategoryName);
            expense.setCategory(category);
        }

        if (expense.getIsTaxDeductible() == null) {
            expense.setIsTaxDeductible(false);
        }

        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id).orElse(null);
    }

    private Category resolveIncomingCategory(ExpenseRequest request) {
        if (request == null) {
            return null;
        }

        if (request.getCategory() != null) {
            Category incomingCategory = request.getCategory();
            String categoryName = normalizeCategoryName(incomingCategory.getName());
            if (categoryName != null) {
                incomingCategory.setName(categoryName);
                return incomingCategory;
            }

            if (incomingCategory.getId() != null) {
                return categoryRepository.findById(incomingCategory.getId()).orElse(null);
            }
        }

        String categoryName = normalizeCategoryName(request.getCategoryName());
        return categoryName != null ? new Category(categoryName) : null;
    }

    private Category getOrCreateCategory(String categoryName) {
        String normalizedName = normalizeCategoryName(categoryName);
        String safeName = normalizedName != null ? normalizedName : "MISCELLANEOUS";
        return categoryRepository.findByNameIgnoreCase(safeName)
            .orElseGet(() -> categoryRepository.save(new Category(safeName)));
    }

    private String normalizeCategoryName(String categoryName) {
        if (categoryName == null) {
            return null;
        }

        String normalized = categoryName.trim();
        return normalized.isEmpty() ? null : normalized.toUpperCase();
    }
}
