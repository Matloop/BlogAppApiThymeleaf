package com.api.BlogAppApi.services;

import com.api.BlogAppApi.models.BlogAppPostModel;
import com.api.BlogAppApi.models.PostComentarioModel;
import com.api.BlogAppApi.repositories.PostComentarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BlogAppServiceImplPostComentario implements BlogAppPostServiceComentarios {

    @Autowired
    private PostComentarioRepository postComentarioRepository;

    @Override
    public PostComentarioModel savePostComentario(PostComentarioModel postComentario) {
        return postComentarioRepository.save(postComentario);
    }

    @Override
    public List<PostComentarioModel> getAllPostComentarios() {
        return postComentarioRepository.findAll();
    }

    @Override
    public List<PostComentarioModel> getComentariosByPostId(UUID postId) {
        return postComentarioRepository.findByPostModelId(postId);
    }

    @Override
    public Optional<PostComentarioModel> getPostComentarioById(UUID id) {
        return postComentarioRepository.findById(id);
    }

    @Override
    public PostComentarioModel updatePostComentario(PostComentarioModel postComentario) {
        return postComentarioRepository.save(postComentario);
    }

    @Override
    public void deletePostComentario(UUID id) {
        postComentarioRepository.deleteById(id);
    }
}