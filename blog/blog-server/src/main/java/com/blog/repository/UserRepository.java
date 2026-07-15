package com.blog.repository;

import com.blog.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository
 * Handles database operations for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by phone number
     */
    Optional<User> findByPhone(String phone);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if phone exists
     */
    boolean existsByPhone(String phone);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Search users by keyword (phone, email, or nickname)
     */
    @Query("SELECT u FROM User u WHERE u.phone LIKE %:keyword% OR u.email LIKE %:keyword% OR u.nickname LIKE %:keyword%")
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Find user by role
     */
    Optional<User> findByRole(User.UserRole role);

    /**
     * Count users created between dates
     */
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
