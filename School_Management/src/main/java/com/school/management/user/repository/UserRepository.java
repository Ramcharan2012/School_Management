package com.school.management.user.repository;

import com.school.management.common.enums.Role;
import com.school.management.common.enums.UserStatus;
import com.school.management.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = :role AND " +
            "(LOWER(u.firstName) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%',:search,'%')))")
    Page<User> searchByRoleAndKeyword(Role role, String search, Pageable pageable);
}
