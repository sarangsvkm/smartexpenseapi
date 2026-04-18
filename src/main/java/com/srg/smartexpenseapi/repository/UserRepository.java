package com.srg.smartexpenseapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.srg.smartexpenseapi.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
  Boolean existsByUsername(String username);
  Boolean existsByEmail(String email);

  // Eagerly fetch roles to avoid LazyInitializationException in admin queries
  @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles")
  List<User> findAllWithRoles();
}
