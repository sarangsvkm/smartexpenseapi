package com.srg.smartexpenseapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.srg.smartexpenseapi.entity.EmiPayment;
import java.util.List;

@Repository
public interface EmiPaymentRepository extends JpaRepository<EmiPayment, Long> {
    List<EmiPayment> findByLoanId(Long loanId);
}
