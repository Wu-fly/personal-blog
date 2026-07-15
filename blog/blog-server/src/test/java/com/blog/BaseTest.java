package com.blog;

import com.blog.config.TestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * 基础测试类
 * 所有测试类都应该继承此类以获得正确的测试配置
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
public abstract class BaseTest {
    // 所有测试类的公共配置
}
