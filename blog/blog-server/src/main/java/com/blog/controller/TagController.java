package com.blog.controller;

import com.blog.dto.ApiResponse;
import com.blog.entity.Tag;
import com.blog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签控制器
 */
@Slf4j
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagRepository tagRepository;

    /**
     * 获取热门标签（使用频率最高的前8个）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Tag>>> getHotTags() {
        log.info("获取热门标签");
        List<Tag> tags = tagRepository.findTopUsedTags(PageRequest.of(0, 8));
        return ResponseEntity.ok(ApiResponse.success(tags));
    }
    
    /**
     * 获取所有标签
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Tag>>> getAllTags() {
        log.info("获取所有标签");
        List<Tag> tags = tagRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(tags));
    }
}
