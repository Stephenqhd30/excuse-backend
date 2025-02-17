package com.stephen.excuse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stephen.excuse.model.dto.post.PostQueryRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * 帖子服务测试
 *
 * @author stephen qiu
 * 
 */
@SpringBootTest
class PostServiceTest {

    @Resource
    private PostEsService postEsService;

    @Test
    void searchFromEs() {
        PostQueryRequest postQueryRequest = new PostQueryRequest();
        postQueryRequest.setUserId(1L);
        Page<Post> postPage = postEsService.searchPostFromEs(postQueryRequest);
        Assertions.assertNotNull(postPage);
    }

}