package com.srg.smartexpenseapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.srg.smartexpenseapi.entity.FinancialSnapshot;
import java.util.List;

@Repository
public interface FinancialSnapshotRepository extends JpaRepository<FinancialSnapshot, Long> {
    List<FinancialSnapshot> findByUserId(Long userId);
}
