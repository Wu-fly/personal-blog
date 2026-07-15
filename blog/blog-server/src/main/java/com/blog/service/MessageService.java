package com.blog.service;

import com.blog.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 私信管理服务接口
 * 需求: 24.1-24.6
 */
public interface MessageService {

    /**
     * 发送私信
     * 需求: 24.2
     * - 保存私信到数据库
     * - 通知接收者
     * - 验证消息内容不为空
     * 
     * @param message 私信对象
     * @return 保存后的私信对象
     */
    Message sendMessage(Message message);

    /**
     * 查询收件箱
     * 需求: 24.3
     * - 返回用户收到的所有私信
     * - 按时间倒序排列
     * - 支持分页
     * 
     * @param receiverId 接收者ID
     * @param pageable 分页参数
     * @return 私信分页列表
     */
    Page<Message> getInbox(Long receiverId, Pageable pageable);

    /**
     * 查询发件箱
     * 需求: 24.3
     * - 返回用户发送的所有私信
     * - 按时间倒序排列
     * - 支持分页
     * 
     * @param senderId 发送者ID
     * @param pageable 分页参数
     * @return 私信分页列表
     */
    Page<Message> getOutbox(Long senderId, Pageable pageable);

    /**
     * 查询对话记录
     * 需求: 24.3
     * - 返回两个用户之间的所有私信
     * - 按时间正序排列
     * 
     * @param userId1 用户1的ID
     * @param userId2 用户2的ID
     * @return 对话记录列表
     */
    List<Message> getConversation(Long userId1, Long userId2);

    /**
     * 标记私信为已读
     * 需求: 24.3
     * - 更新私信的已读状态
     * - 验证权限（只有接收者可以标记）
     * 
     * @param id 私信ID
     * @param userId 当前用户ID
     * @return 更新后的私信对象
     */
    Message markAsRead(Long id, Long userId);

    /**
     * 标记来自某个发送者的所有私信为已读
     * 需求: 24.3
     * - 批量更新私信的已读状态
     * 
     * @param receiverId 接收者ID
     * @param senderId 发送者ID
     */
    void markAllAsReadFromSender(Long receiverId, Long senderId);

    /**
     * 获取未读私信数量
     * 需求: 24.3
     * - 返回用户未读私信的数量
     * 
     * @param receiverId 接收者ID
     * @return 未读私信数量
     */
    long getUnreadCount(Long receiverId);
}
