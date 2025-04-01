package com.api.BlogAppApi.repositories;

import com.api.BlogAppApi.models.BlogAppPostModel;
import org.hibernate.sql.ast.tree.expression.JdbcParameter;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogAppPostRepository extends JpaRepository<BlogAppPostModel, UUID> {

}
