package com.blog.controller;

import com.blog.dto.MessageRequest;
import com.blog.entity.Message;
import com.blog.entity.User;
import com.blog.security.CustomUserDetails;
import com.blog.security.JwtUtil;
import com.blog.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MessageController单元测试
 * 需求: 24.1-24.6
 */
@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    @MockBean
    private JwtUtil jwtUtil;

    private Message testMessage;
    private User testSender;
    private User testReceiver;
    private CustomUserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        // 创建测试发送者
        testSender = new User();
        testSender.setId(1L);
        testSender.setNickname("发送者");
        testSender.setAvatar("sender-avatar.jpg");

        // 创建测试接收者
        testReceiver = new User();
        testReceiver.setId(2L);
        testReceiver.setNickname("接收者");
        testReceiver.setAvatar("receiver-avatar.jpg");

        // 创建测试私信
        testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setSenderId(1L);
        testMessage.setReceiverId(2L);
        testMessage.setContent("这是一条测试私信");
        testMessage.setIsRead(false);
        testMessage.setCreatedAt(LocalDateTime.now());
        testMessage.setSender(testSender);
        testMessage.setReceiver(testReceiver);

        // 创建 CustomUserDetails
        mockUserDetails = new CustomUserDetails(1L, "13800138000", "password", "USER", "ACTIVE");

        // Mock JWT工具
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
    }

    @Test
    void testSendMessage_Success() throws Exception {
        // 准备请求数据
        MessageRequest request = new MessageRequest();
        request.setReceiverId(2L);
        request.setContent("你好，这是一条新私信");

        // Mock服务层
        when(messageService.sendMessage(any(Message.class))).thenReturn(testMessage);

        // 执行请求
        mockMvc.perform(post("/api/messages")
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUserDetails, null, mockUserDetails.getAuthorities())))
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("私信发送成功"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.senderId").value(1))
                .andExpect(jsonPath("$.data.receiverId").value(2))
                .andExpect(jsonPath("$.data.content").value("这是一条测试私信"))
                .andExpect(jsonPath("$.data.isRead").value(false));

        // 验证服务层调用
        verify(messageService, times(1)).sendMessage(any(Message.class));
    }

    @Test
    void testSendMessage_EmptyContent() throws Exception {
        // 准备请求数据（空内容）
        MessageRequest request = new MessageRequest();
        request.setReceiverId(2L);
        request.setContent("");

        // 执行请求
        mockMvc.perform(post("/api/messages")
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUserDetails, null, mockUserDetails.getAuthorities())))
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // 验证服务层未被调用
        verify(messageService, never()).sendMessage(any(Message.class));
    }

    @Test
    void testSendMessage_MissingReceiverId() throws Exception {
        // 准备请求数据（缺少接收者ID）
        MessageRequest request = new MessageRequest();
        request.setContent("测试消息");

        // 执行请求
        mockMvc.perform(post("/api/messages")
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUserDetails, null, mockUserDetails.getAuthorities())))
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // 验证服务层未被调用
        verify(messageService, never()).sendMessage(any(Message.class));
    }

    @Test
    void testGetInbox_Success() throws Exception {
        // 准备测试数据
        List<Message> messages = Arrays.asList(testMessage);
        Page<Message> messagePage = new PageImpl<>(messages);

        // Mock服务层
        when(messageService.getInbox(eq(1L), any(Pageable.class)))
                .thenReturn(messagePage);

        // 执行请求
        mockMvc.perform(get("/api/messages/inbox")
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUserDetails, null, mockUserDetails.getAuthorities())))
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.messages").isArray())
                .andExpect(jsonPath("$.data.messages[0].id").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.currentPage").value(0))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.hasPrevious").value(false));

        // 验证服务层调用
        verify(messageService, times(1)).getInbox(eq(1L), any(Pageable.class));
    }

    @Test
    void testGetConversation_Success() throws Exception {
        // 准备测试数据
        Message message1 = new Message();
        message1.setId(1L);
        message1.setSenderId(1L);
        message1.setReceiverId(2L);
        message1.setContent("消息1");
        message1.setIsRead(true);
        message1.setCreatedAt(LocalDateTime.now().minusHours(2));
        message1.setSender(testSender);
        message1.setReceiver(testReceiver);

        Message message2 = new Message();
        message2.setId(2L);
        message2.setSenderId(2L);
        message2.setReceiverId(1L);
        message2.setContent("消息2");
        message2.setIsRead(false);
        message2.setCreatedAt(LocalDateTime.now().minusHours(1));
        message2.setSender(testReceiver);
        message2.setReceiver(testSender);

        List<Message> conversation = Arrays.asList(message1, message2);

        // Mock服务层
        when(messageService.getConversation(eq(1L), eq(2L))).thenReturn(conversation);

        // 执行请求
        mockMvc.perform(get("/api/messages/conversation/2")
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUserDetails, null, mockUserDetails.getAuthorities()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[1].id").value(2));

        // 验证服务层调用
        verify(messageService, times(1)).getConversation(eq(1L), eq(2L));
    }

    @Test
    void testMarkAsRead_Success() throws Exception {
        // 准备测试数据
        Message readMessage = new Message();
        readMessage.setId(1L);
        readMessage.setSenderId(2L);
        readMessage.setReceiverId(1L);
        readMessage.setContent("这是一条测试私信");
        readMessage.setIsRead(true);
        readMessage.setCreatedAt(LocalDateTime.now());
        readMessage.setSender(testReceiver);
        readMessage.setReceiver(testSender);

        // Mock服务层
        when(messageService.markAsRead(eq(1L), eq(1L))).thenReturn(readMessage);

        // 执行请求
        mockMvc.perform(put("/api/messages/1/read")
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUserDetails, null, mockUserDetails.getAuthorities())))
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("私信已标记为已读"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.isRead").value(true));

        // 验证服务层调用
        verify(messageService, times(1)).markAsRead(eq(1L), eq(1L));
    }

    @Test
    void testMarkAllAsReadFromSender_Success() throws Exception {
        // Mock服务层（void方法）
        doNothing().when(messageService).markAllAsReadFromSender(eq(1L), eq(2L));

        // 执行请求
        mockMvc.perform(put("/api/messages/read-all/2")
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUserDetails, null, mockUserDetails.getAuthorities())))
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("所有私信已标记为已读"));

        // 验证服务层调用
        verify(messageService, times(1)).markAllAsReadFromSender(eq(1L), eq(2L));
    }

    @Test
    void testGetUnreadCount_Success() throws Exception {
        // Mock服务层
        when(messageService.getUnreadCount(eq(1L))).thenReturn(5L);

        // 执行请求
        mockMvc.perform(get("/api/messages/unread-count")
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUserDetails, null, mockUserDetails.getAuthorities()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unreadCount").value(5));

        // 验证服务层调用
        verify(messageService, times(1)).getUnreadCount(eq(1L));
    }

    @Test
    void testGetUnreadCount_Zero() throws Exception {
        // Mock服务层
        when(messageService.getUnreadCount(eq(1L))).thenReturn(0L);

        // 执行请求
        mockMvc.perform(get("/api/messages/unread-count")
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUserDetails, null, mockUserDetails.getAuthorities()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unreadCount").value(0));

        // 验证服务层调用
        verify(messageService, times(1)).getUnreadCount(eq(1L));
    }

    @Test
    void testSendMessage_Unauthorized() throws Exception {
        // 准备请求数据
        MessageRequest request = new MessageRequest();
        request.setReceiverId(2L);
        request.setContent("未授权的私信");

        // 执行请求（未登录）
        mockMvc.perform(post("/api/messages")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        // 验证服务层未被调用
        verify(messageService, never()).sendMessage(any(Message.class));
    }

    @Test
    void testGetInbox_Unauthorized() throws Exception {
        // 执行请求（未登录）
        mockMvc.perform(get("/api/messages/inbox"))
                .andExpect(status().isUnauthorized());

        // 验证服务层未被调用
        verify(messageService, never()).getInbox(anyLong(), any(Pageable.class));
    }

    @Test
    void testGetConversation_Unauthorized() throws Exception {
        // 执行请求（未登录）
        mockMvc.perform(get("/api/messages/conversation/2"))
                .andExpect(status().isUnauthorized());

        // 验证服务层未被调用
        verify(messageService, never()).getConversation(anyLong(), anyLong());
    }

    @Test
    void testMarkAsRead_Unauthorized() throws Exception {
        // 执行请求（未登录）
        mockMvc.perform(put("/api/messages/1/read")
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        // 验证服务层未被调用
        verify(messageService, never()).markAsRead(anyLong(), anyLong());
    }

    @Test
    void testGetInbox_WithPagination() throws Exception {
        // 准备测试数据（多页）
        List<Message> messages = Arrays.asList(testMessage);
        Page<Message> messagePage = new PageImpl<>(messages, 
                org.springframework.data.domain.PageRequest.of(1, 10), 25);

        // Mock服务层
        when(messageService.getInbox(eq(1L), any(Pageable.class)))
                .thenReturn(messagePage);

        // 执行请求
        mockMvc.perform(get("/api/messages/inbox")
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUserDetails, null, mockUserDetails.getAuthorities())))
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentPage").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(3))
                .andExpect(jsonPath("$.data.totalElements").value(25))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.hasPrevious").value(true));

        // 验证服务层调用
        verify(messageService, times(1)).getInbox(eq(1L), any(Pageable.class));
    }
}
