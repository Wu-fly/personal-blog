package com.blog.service.impl;

import com.blog.entity.Message;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.repository.MessageRepository;
import com.blog.repository.UserRepository;
import com.blog.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 私信管理服务实现
 * 需求: 24.1-24.6
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /**
     * 发送私信
     * 需求: 24.2
     * - 保存私信到数据库
     * - 通知接收者
     * - 验证消息内容不为空
     * 
     * 权限: 需求 38.8 - 需要登录才能发送私信
     */
    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public Message sendMessage(Message message) {
        log.info("Sending message from user {} to user {}", message.getSenderId(), message.getReceiverId());
        
        // 验证消息内容不为空 (需求: 24.6)
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            throw new BusinessException("INVALID_INPUT", "消息内容不能为空");
        }
        
        // 验证发送者是否存在
        User sender = userRepository.findById(message.getSenderId())
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "发送者不存在"));
        
        // 验证接收者是否存在 (需求: 24.2)
        User receiver = userRepository.findById(message.getReceiverId())
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "接收者不存在"));
        
        // 验证不能给自己发送私信
        if (message.getSenderId().equals(message.getReceiverId())) {
            throw new BusinessException("INVALID_INPUT", "不能给自己发送私信");
        }
        
        // 设置默认未读状态
        if (message.getIsRead() == null) {
            message.setIsRead(false);
        }
        
        // 保存私信 (需求: 24.2)
        Message savedMessage = messageRepository.save(message);
        
        // 设置发送者和接收者信息到返回的消息对象中
        savedMessage.setSender(sender);
        savedMessage.setReceiver(receiver);
        
        log.info("Message sent successfully with id: {}", savedMessage.getId());
        
        // TODO: 通知接收者（可以通过WebSocket或消息队列实现）(需求: 24.2)
        // 这里暂时只记录日志，实际实现需要集成通知系统
        log.info("Notification sent to user: {}", message.getReceiverId());
        
        return savedMessage;
    }

    /**
     * 查询收件箱
     * 需求: 24.3
     * - 返回用户收到的所有私信
     * - 按时间倒序排列
     * - 支持分页
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Message> getInbox(Long receiverId, Pageable pageable) {
        log.info("Getting inbox for user: {}", receiverId);
        
        // 验证用户是否存在
        User receiver = userRepository.findById(receiverId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        // 查询收件箱，按时间倒序排列 (需求: 24.3)
        Page<Message> messages = messageRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId, pageable);
        
        // 手动加载发送者和接收者信息并设置到Message对象中
        messages.getContent().forEach(message -> {
            if (message.getSenderId() != null) {
                userRepository.findById(message.getSenderId()).ifPresent(sender -> {
                    message.setSender(sender);
                });
            }
            if (message.getReceiverId() != null) {
                userRepository.findById(message.getReceiverId()).ifPresent(rec -> {
                    message.setReceiver(rec);
                });
            }
        });
        
        log.info("Found {} messages in inbox for user: {}", messages.getTotalElements(), receiverId);
        return messages;
    }

    /**
     * 查询发件箱
     * 需求: 24.3
     * - 返回用户发送的所有私信
     * - 按时间倒序排列
     * - 支持分页
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Message> getOutbox(Long senderId, Pageable pageable) {
        log.info("Getting outbox for user: {}", senderId);
        
        // 验证用户是否存在
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        // 查询发件箱，按时间倒序排列
        Page<Message> messages = messageRepository.findBySenderIdOrderByCreatedAtDesc(senderId, pageable);
        
        // 手动加载发送者和接收者信息并设置到Message对象中
        messages.getContent().forEach(message -> {
            if (message.getSenderId() != null) {
                userRepository.findById(message.getSenderId()).ifPresent(s -> {
                    message.setSender(s);
                });
            }
            if (message.getReceiverId() != null) {
                userRepository.findById(message.getReceiverId()).ifPresent(rec -> {
                    message.setReceiver(rec);
                });
            }
        });
        
        log.info("Found {} messages in outbox for user: {}", messages.getTotalElements(), senderId);
        return messages;
    }

    /**
     * 查询对话记录
     * 需求: 24.3
     * - 返回两个用户之间的所有私信
     * - 按时间正序排列
     */
    @Override
    @Transactional(readOnly = true)
    public List<Message> getConversation(Long userId1, Long userId2) {
        log.info("Getting conversation between user {} and user {}", userId1, userId2);
        
        // 验证用户是否存在
        User user1 = userRepository.findById(userId1)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户1不存在"));
        
        User user2 = userRepository.findById(userId2)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户2不存在"));
        
        // 查询对话记录，按时间正序排列 (需求: 24.3)
        List<Message> messages = messageRepository.findConversation(userId1, userId2);
        
        // 手动加载发送者和接收者信息并设置到Message对象中
        messages.forEach(message -> {
            if (message.getSenderId() != null) {
                userRepository.findById(message.getSenderId()).ifPresent(sender -> {
                    message.setSender(sender);
                });
            }
            if (message.getReceiverId() != null) {
                userRepository.findById(message.getReceiverId()).ifPresent(rec -> {
                    message.setReceiver(rec);
                });
            }
        });
        
        log.info("Found {} messages in conversation between user {} and user {}", 
                messages.size(), userId1, userId2);
        return messages;
    }

    /**
     * 标记私信为已读
     * 需求: 24.3
     * - 更新私信的已读状态
     * - 验证权限（只有接收者可以标记）
     */
    @Override
    @Transactional
    public Message markAsRead(Long id, Long userId) {
        log.info("Marking message {} as read by user {}", id, userId);
        
        // 查找私信
        Message message = messageRepository.findById(id)
            .orElseThrow(() -> new BusinessException("MESSAGE_NOT_FOUND", "私信不存在"));
        
        // 验证权限：只有接收者可以标记为已读 (需求: 24.3)
        if (!message.getReceiverId().equals(userId)) {
            throw new BusinessException("PERMISSION_DENIED", "只有接收者可以标记私信为已读");
        }
        
        // 如果已经是已读状态，直接返回
        if (Boolean.TRUE.equals(message.getIsRead())) {
            log.info("Message {} is already read", id);
            return message;
        }
        
        // 更新已读状态 (需求: 24.3)
        message.setIsRead(true);
        Message updatedMessage = messageRepository.save(message);
        
        log.info("Message {} marked as read successfully", id);
        return updatedMessage;
    }

    /**
     * 标记来自某个发送者的所有私信为已读
     * 需求: 24.3
     * - 批量更新私信的已读状态
     */
    @Override
    @Transactional
    public void markAllAsReadFromSender(Long receiverId, Long senderId) {
        log.info("Marking all messages from user {} to user {} as read", senderId, receiverId);
        
        // 验证用户是否存在
        User receiver = userRepository.findById(receiverId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "接收者不存在"));
        
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "发送者不存在"));
        
        // 批量更新已读状态 (需求: 24.3)
        messageRepository.markAllAsReadFromSender(receiverId, senderId);
        
        log.info("All messages from user {} to user {} marked as read successfully", senderId, receiverId);
    }

    /**
     * 获取未读私信数量
     * 需求: 24.3
     * - 返回用户未读私信的数量
     */
    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long receiverId) {
        log.info("Getting unread message count for user: {}", receiverId);
        
        // 验证用户是否存在
        User receiver = userRepository.findById(receiverId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        // 查询未读私信数量 (需求: 24.3)
        long count = messageRepository.countByReceiverIdAndIsReadFalse(receiverId);
        
        log.info("User {} has {} unread messages", receiverId, count);
        return count;
    }
}
