package com.api.BlogAppApi.utils;

import com.api.BlogAppApi.models.BlogAppPost;
import com.api.BlogAppApi.repositories.BlogAppPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataUtils {
    private final BlogAppPostRepository blogAppPostRepository;

    @Autowired
    public DataUtils(BlogAppPostRepository blogAppPostRepository) {
        this.blogAppPostRepository = blogAppPostRepository;
    }

    public void savePosts() {
        List<BlogAppPost> postList = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            postList.add(createPost("Autor " + i, "Título do Post " + i, "Texto do post número " + i + ". Esse é um conteúdo de teste para o blog."));
        }

        blogAppPostRepository.saveAll(postList);

        System.out.println("20 posts foram salvos no banco de dados!");
    }

    private BlogAppPost createPost(String autor, String titulo, String texto) {
        BlogAppPost post = new BlogAppPost();
        post.setAutor(autor);
        post.setData(LocalDateTime.now());
        post.setTitulo(titulo);
        post.setTexto(texto);
        return post;
    }
}
