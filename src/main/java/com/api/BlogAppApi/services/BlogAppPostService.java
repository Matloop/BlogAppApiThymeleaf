package com.api.BlogAppApi.services;

import com.api.BlogAppApi.models.BlogAppPostModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlogAppPostService {
    BlogAppPostModel addBlogAppPost(BlogAppPostModel blogAppPostModel);
    List<BlogAppPostModel> getAllBlogAppPosts();
    Optional<BlogAppPostModel> getBlogAppPostById(UUID id);
    BlogAppPostModel updateBlogAppPost(BlogAppPostModel blogAppPostModel);
    void deleteBlogAppPost(UUID id);
}
