package com.srg.smartexpenseapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.srg.smartexpenseapi.entity.Income;
import com.srg.smartexpenseapi.entity.User;
import com.srg.smartexpenseapi.repository.IncomeRepository;
import java.util.List;

@Service
public class IncomeService {

    @Autowired
    private IncomeRepository incomeRepository;

    public List<Income> getIncomeByUserId(Long userId) {
        return incomeRepository.findByUserId(userId);
    }

    public Income saveIncome(Income income) {
        return incomeRepository.save(income);
    }

    public void deleteIncome(Long id) {
        incomeRepository.deleteById(id);
    }
}
