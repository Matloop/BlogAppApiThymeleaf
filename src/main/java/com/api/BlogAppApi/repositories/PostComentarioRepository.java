package com.api.BlogAppApi.repositories;

import com.api.BlogAppApi.models.PostComentario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostComentarioRepository extends JpaRepository<PostComentario, UUID> {
    List<PostComentario> findByPostModelId(UUID postId);
}
