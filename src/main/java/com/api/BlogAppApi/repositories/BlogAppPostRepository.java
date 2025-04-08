package com.api.BlogAppApi.repositories;

import com.api.BlogAppApi.models.BlogAppPost;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogAppPostRepository extends JpaRepository<BlogAppPost, UUID> {

}
