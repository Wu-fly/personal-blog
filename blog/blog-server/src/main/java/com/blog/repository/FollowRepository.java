package com.blog.repository;

import com.blog.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Follow Repository
 * Handles database operations for Follow entity
 */
@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    
    /**
     * Find follow relationship
     */
    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);
    
    /**
     * Check if user follows another user
     */
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);
    
    /**
     * Find all users that a user is following
     */
    Page<Follow> findByFollowerId(Long followerId, Pageable pageable);
    
    /**
     * Find all followers of a user
     */
    Page<Follow> findByFollowingId(Long followingId, Pageable pageable);
    
    /**
     * Count followers of a user
     */
    long countByFollowingId(Long followingId);
    
    /**
     * Count users that a user is following
     */
    long countByFollowerId(Long followerId);
    
    /**
     * Delete follow relationship
     */
    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
