package com.blog.dto;

import com.blog.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 私信响应DTO
 * 用于返回私信信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    /**
     * 私信ID
     */
    private Long id;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者昵称
     */
    private String senderNickname;

    /**
     * 发送者头像
     */
    private String senderAvatar;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 接收者昵称
     */
    private String receiverNickname;

    /**
     * 接收者头像
     */
    private String receiverAvatar;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 从Message实体转换为MessageResponse
     */
    public static MessageResponse fromEntity(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setSenderId(message.getSenderId());
        response.setReceiverId(message.getReceiverId());
        response.setContent(message.getContent());
        response.setIsRead(message.getIsRead());
        response.setCreatedAt(message.getCreatedAt());
        
        // 设置发送者信息
        if (message.getSender() != null) {
            response.setSenderNickname(message.getSender().getNickname());
            response.setSenderAvatar(message.getSender().getAvatar());
        }
        
        // 设置接收者信息
        if (message.getReceiver() != null) {
            response.setReceiverNickname(message.getReceiver().getNickname());
            response.setReceiverAvatar(message.getReceiver().getAvatar());
        }
        
        return response;
    }

    /**
     * 批量转换
     */
    public static List<MessageResponse> fromEntities(List<Message> messages) {
        return messages.stream()
                .map(MessageResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
