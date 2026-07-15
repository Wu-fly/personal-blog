package com.blog.repository;

import com.blog.entity.SensitiveWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SensitiveWord Repository
 * Handles database operations for SensitiveWord entity
 */
@Repository
public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, Long> {
    
    /**
     * Find sensitive word by word text
     */
    Optional<SensitiveWord> findByWord(String word);
    
    /**
     * Check if word exists
     */
    boolean existsByWord(String word);
    
    /**
     * Find all sensitive words
     */
    @Query("SELECT sw.word FROM SensitiveWord sw")
    List<String> findAllWords();
    
    /**
     * Delete sensitive word by word text
     */
    void deleteByWord(String word);
}
