package com.srg.smartexpenseapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.srg.smartexpenseapi.entity.DebtStrategy;
import java.util.Optional;

@Repository
public interface DebtStrategyRepository extends JpaRepository<DebtStrategy, Long> {
    Optional<DebtStrategy> findByName(String name);
}
