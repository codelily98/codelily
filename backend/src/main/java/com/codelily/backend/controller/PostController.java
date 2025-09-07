package com.codelily.backend.controller;

import com.codelily.backend.domain.Post;
import com.codelily.backend.mapper.PostMapper;
import com.codelily.backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    @GetMapping
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @GetMapping("/top")
    public List<Map<String, Object>> getTopPosts(@RequestParam(defaultValue = "5") int limit) {
        return postMapper.findTopPosts(limit);
    }
}

