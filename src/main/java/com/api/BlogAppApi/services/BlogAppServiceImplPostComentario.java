package com.api.BlogAppApi.services;

import com.api.BlogAppApi.models.PostComentario;
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
    public PostComentario savePostComentario(PostComentario postComentario) {
        return postComentarioRepository.save(postComentario);
    }

    @Override
    public List<PostComentario> getAllPostComentarios() {
        return postComentarioRepository.findAll();
    }

    @Override
    public List<PostComentario> getComentariosByPostId(UUID postId) {
        return postComentarioRepository.findByPostModelId(postId);
    }

    @Override
    public Optional<PostComentario> getPostComentarioById(UUID id) {
        return postComentarioRepository.findById(id);
    }

    @Override
    public PostComentario updatePostComentario(PostComentario postComentario) {
        return postComentarioRepository.save(postComentario);
    }

    @Override
    public void deletePostComentario(UUID id) {
        postComentarioRepository.deleteById(id);
    }
}