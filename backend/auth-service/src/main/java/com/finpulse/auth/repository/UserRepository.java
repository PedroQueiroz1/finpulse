package com.finpulse.auth.repository;

import com.finpulse.auth.entity.User;
import com.finpulse.auth.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByActiveTrue();

    List<User> findByActiveFalse();

    long countByRole(Role role);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.active = true")
    List<User> findActiveUsersByRole(@Param("role") Role role);

    @Query("SELECT u FROM User u WHERE u.createdAt >= :startDate ORDER BY u.createdAt DESC")
    List<User> findUsersCreatedAfter(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> searchByName(@Param("name") String name);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.active = true")
    long countActiveByRole(@Param("role") Role role);

    @Modifying
    @Query("UPDATE User u SET u.active = false, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
    int deactivateUser(@Param("id") UUID id);

    @Query(
        value = "SELECT u.role, COUNT(*) as total, " +
                "SUM(CASE WHEN u.active THEN 1 ELSE 0 END) as active_count " +
                "FROM users u GROUP BY u.role",
        nativeQuery = true
    )
    List<Object[]> getUserStatsByRole();
}