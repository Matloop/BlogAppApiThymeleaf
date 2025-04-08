package com.api.BlogAppApi.services;

import com.api.BlogAppApi.models.PostComentario;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlogAppPostServiceComentarios {
    PostComentario savePostComentario(PostComentario postComentario);
    List<PostComentario> getAllPostComentarios();
    List<PostComentario> getComentariosByPostId(UUID postId);
    Optional<PostComentario> getPostComentarioById(UUID id);
    PostComentario updatePostComentario(PostComentario postComentario);
    void deletePostComentario(UUID id);
}