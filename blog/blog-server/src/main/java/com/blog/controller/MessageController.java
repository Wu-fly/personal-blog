package com.blog.controller;

import com.blog.dto.ApiResponse;
import com.blog.dto.MessageRequest;
import com.blog.dto.MessageResponse;
import com.blog.entity.Message;
import com.blog.security.CustomUserDetails;
import com.blog.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 私信控制器
 * 处理私信发送、接收、对话记录等功能
 * 需求: 24.1-24.6
 */
@Slf4j
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Validated
public class MessageController {

    private final MessageService messageService;

    /**
     * 发送私信
     * POST /api/messages
     * 需求: 24.1, 24.2
     * 
     * @param userDetails 当前登录用户
     * @param request 私信请求
     * @return 发送的私信信息
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MessageRequest request) {
        log.info("User {} sending message to user {}", userDetails.getId(), request.getReceiverId());
        
        // 创建Message实体
        Message message = new Message();
        message.setSenderId(userDetails.getId());
        message.setReceiverId(request.getReceiverId());
        message.setContent(request.getContent());
        
        // 发送私信
        Message sentMessage = messageService.sendMessage(message);
        
        // 转换为响应DTO
        MessageResponse response = MessageResponse.fromEntity(sentMessage);
        
        log.info("Message sent successfully: id={}", sentMessage.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("私信发送成功", response));
    }

    /**
     * 获取收件箱
     * GET /api/messages/inbox
     * 需求: 24.3
     * 
     * @param userDetails 当前登录用户
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 收件箱私信列表（分页）
     */
    @GetMapping("/inbox")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInbox(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Getting inbox for user: {}, page: {}, size: {}", userDetails.getId(), page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagePage = messageService.getInbox(userDetails.getId(), pageable);
        
        // 转换为响应DTO
        List<MessageResponse> messages = MessageResponse.fromEntities(messagePage.getContent());
        
        // 构建分页响应
        Map<String, Object> response = new HashMap<>();
        response.put("messages", messages);
        response.put("currentPage", messagePage.getNumber());
        response.put("totalPages", messagePage.getTotalPages());
        response.put("totalElements", messagePage.getTotalElements());
        response.put("hasNext", messagePage.hasNext());
        response.put("hasPrevious", messagePage.hasPrevious());
        
        log.info("Found {} messages in inbox for user: {}", messagePage.getTotalElements(), userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取发件箱
     * GET /api/messages/outbox
     * 需求: 24.3
     * 
     * @param userDetails 当前登录用户
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 发件箱私信列表（分页）
     */
    @GetMapping("/outbox")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOutbox(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Getting outbox for user: {}, page: {}, size: {}", userDetails.getId(), page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagePage = messageService.getOutbox(userDetails.getId(), pageable);
        
        // 转换为响应DTO
        List<MessageResponse> messages = MessageResponse.fromEntities(messagePage.getContent());
        
        // 构建分页响应
        Map<String, Object> response = new HashMap<>();
        response.put("messages", messages);
        response.put("currentPage", messagePage.getNumber());
        response.put("totalPages", messagePage.getTotalPages());
        response.put("totalElements", messagePage.getTotalElements());
        response.put("hasNext", messagePage.hasNext());
        response.put("hasPrevious", messagePage.hasPrevious());
        
        log.info("Found {} messages in outbox for user: {}", messagePage.getTotalElements(), userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取对话记录
     * GET /api/messages/conversation/{userId}
     * 需�? 24.3, 24.5
     * 
     * @param userDetails 当前登录用户
     * @param userId 对话用户ID
     * @return 对话记录列表
     */
    @GetMapping("/conversation/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getConversation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId) {
        log.info("Getting conversation between user {} and user {}", userDetails.getId(), userId);
        
        // 获取对话记录
        List<Message> messages = messageService.getConversation(userDetails.getId(), userId);
        
        // 转换为响应DTO
        List<MessageResponse> response = MessageResponse.fromEntities(messages);
        
        log.info("Found {} messages in conversation", messages.size());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 标记私信为已�?
     * PUT /api/messages/{id}/read
     * 需�? 24.3
     * 
     * @param userDetails 当前登录用户
     * @param id 私信ID
     * @return 更新后的私信信息
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MessageResponse>> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        log.info("User {} marking message {} as read", userDetails.getId(), id);
        
        // 标记为已读
        Message message = messageService.markAsRead(id, userDetails.getId());
        
        // 转换为响应DTO
        MessageResponse response = MessageResponse.fromEntity(message);
        
        log.info("Message {} marked as read successfully", id);
        return ResponseEntity.ok(ApiResponse.success("私信已标记为已读", response));
    }

    /**
     * 标记来自某个用户的所有私信为已读
     * PUT /api/messages/read-all/{senderId}
     * 需求: 24.3
     * 
     * @param userDetails 当前登录用户
     * @param senderId 发送者ID
     * @return 成功消息
     */
    @PutMapping("/read-all/{senderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAllAsReadFromSender(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long senderId) {
        log.info("User {} marking all messages from user {} as read", userDetails.getId(), senderId);
        
        // 批量标记为已读
        messageService.markAllAsReadFromSender(userDetails.getId(), senderId);
        
        log.info("All messages from user {} to user {} marked as read", senderId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("所有私信已标记为已读", null));
    }

    /**
     * 获取未读私信数量
     * GET /api/messages/unread-count
     * 需求: 24.3
     * 
     * @param userDetails 当前登录用户
     * @return 未读私信数量
     */
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Getting unread message count for user: {}", userDetails.getId());
        
        long count = messageService.getUnreadCount(userDetails.getId());
        
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        
        log.info("User {} has {} unread messages", userDetails.getId(), count);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

