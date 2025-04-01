package com.api.BlogAppApi.controllers;

import com.api.BlogAppApi.dtos.BlogAppRecordDto;
import com.api.BlogAppApi.dtos.BlogAppRecordDtoComentario;
import com.api.BlogAppApi.models.BlogAppPostModel;
import com.api.BlogAppApi.models.PostComentarioModel;
import com.api.BlogAppApi.services.BlogAppPostService;
import com.api.BlogAppApi.services.BlogAppPostServiceComentarios;
import jakarta.validation.Valid;
import jakarta.websocket.OnClose;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/blog")
public class BlogAppPostController {

    private final BlogAppPostService blogAppPostService;
    private final BlogAppPostServiceComentarios blogAppPostServiceComentarios;

    @Autowired
    public BlogAppPostController(BlogAppPostService blogAppPostService, BlogAppPostServiceComentarios blogAppPostServiceComentarios) {
        this.blogAppPostService = blogAppPostService;
        this.blogAppPostServiceComentarios = blogAppPostServiceComentarios;
    }

    // Endpoint para adicionar um novo post
    @PostMapping
    public ResponseEntity<BlogAppPostModel> addBlogAppPost(@RequestBody BlogAppPostModel blogAppPostModel) {
        BlogAppPostModel savedPost = blogAppPostService.addBlogAppPost(blogAppPostModel);
        return new ResponseEntity<>(savedPost, HttpStatus.CREATED);
    }

    @PostMapping("/newpost")
    public ResponseEntity<Object> savePost(@RequestBody @Valid BlogAppRecordDto blogAppRecordDto) {
        // Converte o DTO para o modelo de entidade
        BlogAppPostModel postModel = new BlogAppPostModel();
        BeanUtils.copyProperties(blogAppRecordDto, postModel);

        // Define a data atual (início do dia em UTC)
        postModel.setData(LocalDateTime.now());

        // Persiste o post e retorna o objeto salvo com status 201 (Created)
        BlogAppPostModel savedPost = blogAppPostService.addBlogAppPost(postModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPost);
    }

    // Endpoint para listar todos os posts
    @GetMapping
    public ResponseEntity<List<BlogAppPostModel>> getAllBlogAppPosts() {
        List<BlogAppPostModel> posts = blogAppPostService.getAllBlogAppPosts();
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    // Endpoint para buscar um post por ID
    @GetMapping("/{id}")
    public ResponseEntity<BlogAppPostModel> getBlogAppPostById(@PathVariable UUID id) {
        Optional<BlogAppPostModel> post = blogAppPostService.getBlogAppPostById(id);
        return post.map(blogAppPostModel -> new ResponseEntity<>(blogAppPostModel, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Endpoint para atualizar um post existente
    @PutMapping("/{id}")
    public ResponseEntity<BlogAppPostModel> updateBlogAppPost(@PathVariable UUID id, @RequestBody BlogAppPostModel blogAppPostModel) {
        BlogAppPostModel updatedPost = blogAppPostService.updateBlogAppPost(blogAppPostModel);
        return updatedPost != null ? new ResponseEntity<>(updatedPost, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Endpoint para deletar um post por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlogAppPost(@PathVariable UUID id) {
        blogAppPostService.deleteBlogAppPost(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        Optional<BlogAppPostModel> blogAppModelOptional = blogAppPostService.getBlogAppPostById(id);

        if (blogAppModelOptional.isEmpty()) {
            // Se o post não for encontrado, retorna 404 NOT FOUND
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Se encontrado, deleta o post pelo id e retorna 204 NO CONTENT
        blogAppPostService.deleteBlogAppPost(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("editpost/{id}")
    public ResponseEntity<BlogAppPostModel> editPost(@PathVariable UUID id, @RequestBody BlogAppPostModel blogAppPostModel) {
        Optional<BlogAppPostModel> blogAppModelOptional = blogAppPostService.getBlogAppPostById(id);

        if (blogAppModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Garante que o ID do corpo seja o mesmo da URL
        blogAppPostModel.setId(id);

        BlogAppPostModel updatedPost = blogAppPostService.updateBlogAppPost(blogAppPostModel);
        return ResponseEntity.ok(updatedPost);
    }

    @PostMapping("/posts/{id}")
    public ResponseEntity<Object> saveComentarioPost(@PathVariable("id") UUID id,
                                                     @RequestBody @Valid BlogAppRecordDtoComentario blogAppRecordDtoComentario) {
        Optional<BlogAppPostModel> blogAppPostModelOptional = blogAppPostService.getBlogAppPostById(id);
        if (blogAppPostModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post não encontrado");
        }

        PostComentarioModel postComentario = new PostComentarioModel();
        BlogAppPostModel postModel = blogAppPostModelOptional.get();
        BeanUtils.copyProperties(blogAppRecordDtoComentario, postComentario);
        postComentario.setPostModel(postModel);
        postComentario.setDate(LocalDateTime.now(ZoneId.of("UTC")));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(blogAppPostServiceComentarios.savePostComentario(postComentario));
    }

    // NOVOS ENDPOINTS

    // Listar posts com seus comentários
    @GetMapping("/posts-with-comments")
    public ResponseEntity<List<Map<String, Object>>> getPostsWithComments() {
        List<BlogAppPostModel> posts = blogAppPostService.getAllBlogAppPosts();
        List<Map<String, Object>> postsWithComments = posts.stream()
                .map(post -> {
                    Map<String, Object> postMap = new HashMap<>();
                    postMap.put("post", post);
                    postMap.put("comentarios", blogAppPostServiceComentarios.getComentariosByPostId(post.getId()));
                    return postMap;
                })
                .toList();

        return ResponseEntity.ok(postsWithComments);
    }

    // Listar comentários de um post específico
    @GetMapping("/posts/{postId}/comentarios")
    public ResponseEntity<List<PostComentarioModel>> getComentariosByPostId(@PathVariable UUID postId) {
        Optional<BlogAppPostModel> blogAppPostModelOptional = blogAppPostService.getBlogAppPostById(postId);

        if (blogAppPostModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<PostComentarioModel> comentarios = blogAppPostServiceComentarios.getComentariosByPostId(postId);
        return ResponseEntity.ok(comentarios);
    }

    // Obter um comentário específico de um post
    @GetMapping("/posts/{postId}/comentarios/{comentarioId}")
    public ResponseEntity<PostComentarioModel> getComentarioById(
            @PathVariable UUID postId,
            @PathVariable UUID comentarioId) {

        Optional<BlogAppPostModel> blogAppPostModelOptional = blogAppPostService.getBlogAppPostById(postId);
        if (blogAppPostModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Optional<PostComentarioModel> comentarioOptional = blogAppPostServiceComentarios.getPostComentarioById(comentarioId);
        if (comentarioOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        PostComentarioModel comentario = comentarioOptional.get();

        // Verifica se o comentário pertence ao post especificado
        if (!comentario.getPostModel().getId().equals(postId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(comentario);
    }

    // Editar um comentário de um post
    @PutMapping("/posts/{postId}/comentarios/{comentarioId}")
    public ResponseEntity<PostComentarioModel> updateComentario(
            @PathVariable UUID postId,
            @PathVariable UUID comentarioId,
            @RequestBody @Valid BlogAppRecordDtoComentario blogAppRecordDtoComentario) {

        Optional<BlogAppPostModel> blogAppPostModelOptional = blogAppPostService.getBlogAppPostById(postId);
        if (blogAppPostModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Optional<PostComentarioModel> comentarioOptional = blogAppPostServiceComentarios.getPostComentarioById(comentarioId);
        if (comentarioOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        PostComentarioModel comentario = comentarioOptional.get();

        // Verifica se o comentário pertence ao post especificado
        if (!comentario.getPostModel().getId().equals(postId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Atualiza apenas o conteúdo do comentário
        comentario.setComentario(blogAppRecordDtoComentario.comentario());

        PostComentarioModel updatedComentario = blogAppPostServiceComentarios.updatePostComentario(comentario);
        return ResponseEntity.ok(updatedComentario);
    }

    // Deletar um comentário de um post
    @DeleteMapping("/posts/{postId}/comentarios/{comentarioId}")
    public ResponseEntity<Void> deleteComentario(
            @PathVariable UUID postId,
            @PathVariable UUID comentarioId) {

        Optional<BlogAppPostModel> blogAppPostModelOptional = blogAppPostService.getBlogAppPostById(postId);
        if (blogAppPostModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Optional<PostComentarioModel> comentarioOptional = blogAppPostServiceComentarios.getPostComentarioById(comentarioId);
        if (comentarioOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        PostComentarioModel comentario = comentarioOptional.get();

        // Verifica se o comentário pertence ao post especificado
        if (!comentario.getPostModel().getId().equals(postId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        blogAppPostServiceComentarios.deletePostComentario(comentarioId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // Corrigindo o método existente que tinha erros
    @GetMapping("/listpost/{id}")
    public ResponseEntity<List<PostComentarioModel>> getPostComentarios(@PathVariable("id") UUID id) {
        Optional<BlogAppPostModel> blogAppPostModelOptional = blogAppPostService.getBlogAppPostById(id);

        if (blogAppPostModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<PostComentarioModel> comentarios = blogAppPostServiceComentarios.getComentariosByPostId(id);
        return ResponseEntity.ok(comentarios);
    }

    // Corrigindo o método existente que tinha erros
    @GetMapping("/post/{postId}/comentario/{comentarioId}")
    public ResponseEntity<PostComentarioModel> getPostComentariosById(
            @PathVariable("postId") UUID postId,
            @PathVariable("comentarioId") UUID comentarioId) {

        Optional<BlogAppPostModel> blogAppPostModelOptional = blogAppPostService.getBlogAppPostById(postId);
        if (blogAppPostModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Optional<PostComentarioModel> comentarioOptional = blogAppPostServiceComentarios.getPostComentarioById(comentarioId);
        if (comentarioOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        PostComentarioModel comentario = comentarioOptional.get();

        // Verifica se o comentário pertence ao post especificado
        if (!comentario.getPostModel().getId().equals(postId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(comentario);
    }
}