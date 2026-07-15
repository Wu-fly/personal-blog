package com.blog.repository;

import com.blog.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Announcement Repository
 * Handles database operations for Announcement entity
 */
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    
    /**
     * Find announcement by user ID (blogger's announcement)
     */
    Optional<Announcement> findByUserId(Long userId);
    
    /**
     * Check if user has an announcement
     */
    boolean existsByUserId(Long userId);
    
    /**
     * Delete announcement by user ID
     */
    void deleteByUserId(Long userId);
}
