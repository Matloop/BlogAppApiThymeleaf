package com.api.BlogAppApi.repositories;

import com.api.BlogAppApi.models.PostComentarioModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostComentarioRepository extends JpaRepository<PostComentarioModel, UUID> {
    List<PostComentarioModel> findByPostModelId(UUID postId);
}
