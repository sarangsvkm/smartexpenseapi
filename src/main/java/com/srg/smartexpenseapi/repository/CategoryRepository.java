package com.srg.smartexpenseapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.srg.smartexpenseapi.entity.Category;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    Optional<Category> findByNameIgnoreCase(String name);
}
