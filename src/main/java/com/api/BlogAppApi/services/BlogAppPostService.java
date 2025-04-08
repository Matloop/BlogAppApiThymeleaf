package com.api.BlogAppApi.services;

import com.api.BlogAppApi.models.BlogAppPost;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlogAppPostService {
    BlogAppPost addBlogAppPost(BlogAppPost blogAppPost);
    List<BlogAppPost> getAllBlogAppPosts();
    Optional<BlogAppPost> getBlogAppPostById(UUID id);
    BlogAppPost updateBlogAppPost(BlogAppPost blogAppPost);
    void deleteBlogAppPost(UUID id);
}
