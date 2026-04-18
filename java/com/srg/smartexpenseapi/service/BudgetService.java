package com.srg.smartexpenseapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.srg.smartexpenseapi.entity.Budget;
import com.srg.smartexpenseapi.entity.User;
import com.srg.smartexpenseapi.repository.BudgetRepository;
import java.util.List;
import java.util.Optional;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    public List<Budget> getBudgetsByUser(User user) {
        return budgetRepository.findByUser(user);
    }

    public Optional<Budget> getBudgetByUserAndMonth(Long userId, Integer month, Integer year) {
        return budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
    }

    public Budget saveBudget(Budget budget) {
        return budgetRepository.save(budget);
    }

    public void deleteBudget(Long id) {
        budgetRepository.deleteById(id);
    }
}
