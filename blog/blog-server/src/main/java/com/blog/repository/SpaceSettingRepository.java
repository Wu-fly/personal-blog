package com.blog.repository;

import com.blog.entity.SpaceSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * SpaceSetting Repository
 * Handles database operations for SpaceSetting entity
 */
@Repository
public interface SpaceSettingRepository extends JpaRepository<SpaceSetting, Long> {
    
    /**
     * Find space setting by user ID
     */
    Optional<SpaceSetting> findByUserId(Long userId);
    
    /**
     * Check if user has space settings
     */
    boolean existsByUserId(Long userId);
    
    /**
     * Delete space setting by user ID
     */
    void deleteByUserId(Long userId);
}
