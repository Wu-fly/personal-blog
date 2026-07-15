package com.blog.service;

import com.blog.entity.Message;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.repository.MessageRepository;
import com.blog.repository.UserRepository;
import com.blog.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * MessageService单元测试
 * 需求: 24.1-24.6
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageServiceImpl messageService;

    private User sender;
    private User receiver;
    private Message message;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        sender = new User();
        sender.setId(1L);
        sender.setNickname("发送者");
        sender.setPhone("13800138001");
        sender.setEmail("sender@test.com");

        receiver = new User();
        receiver.setId(2L);
        receiver.setNickname("接收者");
        receiver.setPhone("13800138002");
        receiver.setEmail("receiver@test.com");

        // 创建测试私信
        message = new Message();
        message.setSenderId(1L);
        message.setReceiverId(2L);
        message.setContent("测试消息");
        message.setIsRead(false);
    }

    /**
     * 测试发送私信成功
     * 需求: 24.2
     */
    @Test
    void testSendMessage_Success() {
        // 准备数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message msg = invocation.getArgument(0);
            msg.setId(1L);
            return msg;
        });

        // 执行测试
        Message result = messageService.sendMessage(message);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试消息", result.getContent());
        assertFalse(result.getIsRead());
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    /**
     * 测试发送空消息失败
     * 需求: 24.6
     */
    @Test
    void testSendMessage_EmptyContent() {
        // 准备数据
        message.setContent("");

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            messageService.sendMessage(message);
        });

        assertEquals("INVALID_INPUT", exception.getErrorCode());
        assertEquals("消息内容不能为空", exception.getMessage());
        verify(messageRepository, never()).save(any(Message.class));
    }

    /**
     * 测试发送消息给不存在的用户失败
     * 需求: 24.2
     */
    @Test
    void testSendMessage_ReceiverNotFound() {
        // 准备数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            messageService.sendMessage(message);
        });

        assertEquals("USER_NOT_FOUND", exception.getErrorCode());
        assertEquals("接收者不存在", exception.getMessage());
        verify(messageRepository, never()).save(any(Message.class));
    }

    /**
     * 测试不能给自己发送私信
     * 需求: 24.2
     */
    @Test
    void testSendMessage_SelfMessage() {
        // 准备数据
        message.setReceiverId(1L); // 发送者和接收者相同
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            messageService.sendMessage(message);
        });

        assertEquals("INVALID_INPUT", exception.getErrorCode());
        assertEquals("不能给自己发送私信", exception.getMessage());
        verify(messageRepository, never()).save(any(Message.class));
    }

    /**
     * 测试查询收件箱成功
     * 需求: 24.3
     */
    @Test
    void testGetInbox_Success() {
        // 准备数据
        Pageable pageable = PageRequest.of(0, 10);
        List<Message> messages = Arrays.asList(message);
        Page<Message> messagePage = new PageImpl<>(messages, pageable, 1);

        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(messageRepository.findByReceiverIdOrderByCreatedAtDesc(2L, pageable))
            .thenReturn(messagePage);

        // 执行测试
        Page<Message> result = messageService.getInbox(2L, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(message, result.getContent().get(0));
        verify(messageRepository, times(1)).findByReceiverIdOrderByCreatedAtDesc(2L, pageable);
    }

    /**
     * 测试查询对话记录成功
     * 需求: 24.3
     */
    @Test
    void testGetConversation_Success() {
        // 准备数据
        Message message2 = new Message();
        message2.setId(2L);
        message2.setSenderId(2L);
        message2.setReceiverId(1L);
        message2.setContent("回复消息");

        List<Message> messages = Arrays.asList(message, message2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(messageRepository.findConversation(1L, 2L)).thenReturn(messages);

        // 执行测试
        List<Message> result = messageService.getConversation(1L, 2L);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(messageRepository, times(1)).findConversation(1L, 2L);
    }

    /**
     * 测试标记私信为已读成功
     * 需求: 24.3
     */
    @Test
    void testMarkAsRead_Success() {
        // 准备数据
        message.setId(1L);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 执行测试
        Message result = messageService.markAsRead(1L, 2L);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getIsRead());
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    /**
     * 测试非接收者标记私信为已读失败
     * 需求: 24.3
     */
    @Test
    void testMarkAsRead_PermissionDenied() {
        // 准备数据
        message.setId(1L);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        // 执行测试并验证异常（用户3不是接收者）
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            messageService.markAsRead(1L, 3L);
        });

        assertEquals("PERMISSION_DENIED", exception.getErrorCode());
        assertEquals("只有接收者可以标记私信为已读", exception.getMessage());
        verify(messageRepository, never()).save(any(Message.class));
    }

    /**
     * 测试标记已读的私信不重复保存
     * 需求: 24.3
     */
    @Test
    void testMarkAsRead_AlreadyRead() {
        // 准备数据
        message.setId(1L);
        message.setIsRead(true); // 已经是已读状态
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        // 执行测试
        Message result = messageService.markAsRead(1L, 2L);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.getIsRead());
        verify(messageRepository, never()).save(any(Message.class)); // 不应该保存
    }

    /**
     * 测试批量标记已读成功
     * 需求: 24.3
     */
    @Test
    void testMarkAllAsReadFromSender_Success() {
        // 准备数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        doNothing().when(messageRepository).markAllAsReadFromSender(2L, 1L);

        // 执行测试
        messageService.markAllAsReadFromSender(2L, 1L);

        // 验证结果
        verify(messageRepository, times(1)).markAllAsReadFromSender(2L, 1L);
    }

    /**
     * 测试获取未读消息数量成功
     * 需求: 24.3
     */
    @Test
    void testGetUnreadCount_Success() {
        // 准备数据
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(messageRepository.countByReceiverIdAndIsReadFalse(2L)).thenReturn(5L);

        // 执行测试
        long count = messageService.getUnreadCount(2L);

        // 验证结果
        assertEquals(5L, count);
        verify(messageRepository, times(1)).countByReceiverIdAndIsReadFalse(2L);
    }

    /**
     * 测试查询不存在用户的收件箱失败
     * 需求: 24.3
     */
    @Test
    void testGetInbox_UserNotFound() {
        // 准备数据
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            messageService.getInbox(999L, pageable);
        });

        assertEquals("USER_NOT_FOUND", exception.getErrorCode());
        verify(messageRepository, never()).findByReceiverIdOrderByCreatedAtDesc(anyLong(), any(Pageable.class));
    }
}
