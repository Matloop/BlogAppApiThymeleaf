package com.api.BlogAppApi.services;

import com.api.BlogAppApi.models.BlogAppPostModel;
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
    public BlogAppPostModel addBlogAppPost(BlogAppPostModel blogAppPostModel) {
        return postRepository.save(blogAppPostModel);
    }

    @Override
    public List<BlogAppPostModel> getAllBlogAppPosts() {
        return postRepository.findAll();
    }

    @Override
    public Optional<BlogAppPostModel> getBlogAppPostById(UUID id) {
        return postRepository.findById(id);
    }

    @Override
    public BlogAppPostModel updateBlogAppPost(BlogAppPostModel blogAppPostModel) {
        return postRepository.save(blogAppPostModel);
    }

    @Override
    public void deleteBlogAppPost(UUID id) {
        postRepository.deleteById(id);
    }
}
