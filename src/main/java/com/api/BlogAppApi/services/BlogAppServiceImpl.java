package com.api.BlogAppApi.services;

import com.api.BlogAppApi.models.BlogAppPost;
import com.api.BlogAppApi.repositories.BlogAppPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BlogAppServiceImpl implements BlogAppPostService {

    private final BlogAppPostRepository postRepository;

    @Autowired
    public BlogAppServiceImpl(BlogAppPostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public BlogAppPost addBlogAppPost(BlogAppPost blogAppPost) {
        return postRepository.save(blogAppPost);
    }

    @Override
    public List<BlogAppPost> getAllBlogAppPosts() {
        return postRepository.findAll();
    }

    @Override
    public Optional<BlogAppPost> getBlogAppPostById(UUID id) {
        return postRepository.findById(id);
    }

    @Override
    public BlogAppPost updateBlogAppPost(BlogAppPost blogAppPost) {
        return postRepository.save(blogAppPost);
    }

    @Override
    public void deleteBlogAppPost(UUID id) {
        postRepository.deleteById(id);
    }
}
