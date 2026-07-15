package com.blog.config;

import com.blog.service.SmsService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 测试配置类
 * 提供测试环境所需的Mock Bean
 */
@TestConfiguration
public class TestConfig {

    /**
     * 提供Mock的StringRedisTemplate用于测试
     * 使用内存Map模拟Redis行为
     */
    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = Mockito.mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);
        
        // 使用内存Map模拟Redis存储
        Map<String, String> storage = new ConcurrentHashMap<>();
        
        // Mock opsForValue()
        Mockito.when(template.opsForValue()).thenReturn(valueOps);
        
        // Mock set操作
        Mockito.doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            String value = invocation.getArgument(1);
            storage.put(key, value);
            return null;
        }).when(valueOps).set(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.any(TimeUnit.class));
        
        // Mock get操作
        Mockito.when(valueOps.get(Mockito.anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return storage.get(key);
        });
        
        // Mock delete操作
        Mockito.when(template.delete(Mockito.anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return storage.remove(key) != null;
        });
        
        return template;
    }
}
