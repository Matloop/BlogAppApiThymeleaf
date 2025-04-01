package com.api.BlogAppApi.services;

import com.api.BlogAppApi.models.PostComentarioModel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlogAppPostServiceComentarios {
    PostComentarioModel savePostComentario(PostComentarioModel postComentario);
    List<PostComentarioModel> getAllPostComentarios();
    List<PostComentarioModel> getComentariosByPostId(UUID postId);
    Optional<PostComentarioModel> getPostComentarioById(UUID id);
    PostComentarioModel updatePostComentario(PostComentarioModel postComentario);
    void deletePostComentario(UUID id);
}