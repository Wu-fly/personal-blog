package com.blog.repository;

import com.blog.entity.BloggerApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * BloggerApplication Repository
 * Handles database operations for BloggerApplication entity
 */
@Repository
public interface BloggerApplicationRepository extends JpaRepository<BloggerApplication, Long> {
    
    /**
     * Find application by user ID
     */
    Optional<BloggerApplication> findByUserId(Long userId);
    
    /**
     * Find applications by status
     */
    Page<BloggerApplication> findByStatusOrderByCreatedAtDesc(BloggerApplication.ApplicationStatus status, Pageable pageable);
    
    /**
     * Find all applications ordered by creation date
     */
    Page<BloggerApplication> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Check if user has pending application
     */
    boolean existsByUserIdAndStatus(Long userId, BloggerApplication.ApplicationStatus status);
}
