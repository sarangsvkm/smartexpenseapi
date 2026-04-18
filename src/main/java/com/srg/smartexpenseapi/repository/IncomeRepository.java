package com.srg.smartexpenseapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.srg.smartexpenseapi.entity.Income;
import com.srg.smartexpenseapi.entity.User;
import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findByUser(User user);
    List<Income> findByUserId(Long userId);
}
