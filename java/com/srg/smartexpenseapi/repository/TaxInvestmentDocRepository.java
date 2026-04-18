package com.srg.smartexpenseapi.repository;

import com.srg.smartexpenseapi.entity.TaxInvestmentDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaxInvestmentDocRepository extends JpaRepository<TaxInvestmentDoc, Long> {
    List<TaxInvestmentDoc> findByUserIdAndFiscalYear(Long userId, Integer fiscalYear);
    List<TaxInvestmentDoc> findByUserId(Long userId);
}
