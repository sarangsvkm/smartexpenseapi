package com.srg.smartexpenseapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.srg.smartexpenseapi.entity.Expense;
import com.srg.smartexpenseapi.entity.User;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"category"})
    List<Expense> findByUser(User user);

    @org.springframework.data.jpa.repository.Query("SELECT e FROM Expense e JOIN FETCH e.category WHERE e.user.id = :userId")
    List<Expense> findByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);
}
