package com.blog.repository;

import com.blog.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Message Repository
 * Handles database operations for Message entity
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * Find messages received by a user (with sender info)
     */
    @Query("SELECT m FROM Message m WHERE m.receiverId = :receiverId ORDER BY m.createdAt DESC")
    Page<Message> findByReceiverIdOrderByCreatedAtDesc(@Param("receiverId") Long receiverId, Pageable pageable);
    
    /**
     * Find messages sent by a user (with receiver info)
     */
    @Query("SELECT m FROM Message m WHERE m.senderId = :senderId ORDER BY m.createdAt DESC")
    Page<Message> findBySenderIdOrderByCreatedAtDesc(@Param("senderId") Long senderId, Pageable pageable);
    
    /**
     * Find conversation between two users
     */
    @Query("SELECT m FROM Message m WHERE (m.senderId = :userId1 AND m.receiverId = :userId2) OR (m.senderId = :userId2 AND m.receiverId = :userId1) ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    /**
     * Count unread messages for a user
     */
    long countByReceiverIdAndIsReadFalse(Long receiverId);
    
    /**
     * Mark message as read
     */
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.id = :id")
    void markAsRead(@Param("id") Long id);
    
    /**
     * Mark all messages from a sender as read
     */
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.receiverId = :receiverId AND m.senderId = :senderId")
    void markAllAsReadFromSender(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);
}
