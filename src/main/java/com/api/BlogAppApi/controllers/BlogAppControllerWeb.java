package com.api.BlogAppApi.controllers;

import com.api.BlogAppApi.dtos.BlogAppRecordDto; // Importar o DTO
import com.api.BlogAppApi.models.BlogAppPost;
import com.api.BlogAppApi.models.PostComentario;
import com.api.BlogAppApi.services.BlogAppPostService;
import com.api.BlogAppApi.services.BlogAppPostServiceComentarios;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/web")
public class BlogAppControllerWeb {

    private static final Logger log = LoggerFactory.getLogger(BlogAppControllerWeb.class);

    private final BlogAppPostService blogAppPostService;
    private final BlogAppPostServiceComentarios blogAppPostServiceComentarios;

    @Autowired
    // Injeção das classes services
    public BlogAppControllerWeb(BlogAppPostService blogAppPostService,
                                BlogAppPostServiceComentarios blogAppPostServiceComentarios) {
        this.blogAppPostService = blogAppPostService;
        this.blogAppPostServiceComentarios = blogAppPostServiceComentarios;
    }

    //Retornar todos os posts
    @GetMapping("/posts")
    public ModelAndView getPosts() {
        //Criar e retornar modelo de model and view
        ModelAndView modelAndView = new ModelAndView("posts");
        try {
            List<BlogAppPost> posts = blogAppPostService.getAllBlogAppPosts()
                    .stream()
                    .sorted(Comparator.comparing(BlogAppPost::getData).reversed())
                    .collect(Collectors.toList());
            modelAndView.addObject("posts", posts);
        } catch (Exception e) {
            log.error("Erro ao buscar posts para /web/posts", e);
            modelAndView.addObject("globalError", "Não foi possível carregar os posts.");
        }
        return modelAndView;
    }

    //Mostrar o formulário para o usuário preencher
    @GetMapping(value = "/newpost")
    public String getPostForm(Model model) {
        model.addAttribute("post", new BlogAppRecordDto("", "", ""));
        return "newPostForm";
    }
    //Envio de dados do form para database
    @PostMapping(value = "/newpost")
    public String savePost(@Valid @ModelAttribute("post") BlogAppRecordDto postDto,
                           BindingResult bindingResult,
                           RedirectAttributes attributes,
                           Model model) {

        if (bindingResult.hasErrors()) {
            // Se der errado os dados do usuário continuam preechidos
            model.addAttribute("post", postDto);
            return "newPostForm";
        }

        BlogAppPost newPost = new BlogAppPost();
        newPost.setAutor(postDto.autor());
        newPost.setTitulo(postDto.titulo());
        newPost.setTexto(postDto.texto());
        //Colocar data atual na ho post
        newPost.setData(LocalDateTime.now());

        //Exibir mensagens de erro ou sucesso na hora da criação do post
        try {
            blogAppPostService.addBlogAppPost(newPost);
            attributes.addFlashAttribute("message", "Post criado com sucesso!");
            return "redirect:/web/posts";
        } catch (Exception e) {
            log.error("Erro ao salvar novo post", e);
            attributes.addFlashAttribute("error", "Erro ao salvar o post.");
            return "redirect:/web/newpost";
        }
    }
    //Mostrar formulário de edição
    @GetMapping("/posts/edit/{id}")
    public String getEditPostForm(@PathVariable UUID id, Model model, RedirectAttributes attributes) {
        //Buscar post na database
        Optional<BlogAppPost> postOptional = blogAppPostService.getBlogAppPostById(id);
        //Se existir mandar os dados, se não pop up na tela
        if (postOptional.isPresent()) {
            BlogAppPost postEntity = postOptional.get();
            BlogAppRecordDto postDto = new BlogAppRecordDto(
                    postEntity.getAutor(),
                    postEntity.getTitulo(),
                    postEntity.getTexto()
            );
            model.addAttribute("post", postDto);
            model.addAttribute("postId", postEntity.getId());
            model.addAttribute("postDate", postEntity.getData());
            return "editPostForm";
        } else {
            attributes.addFlashAttribute("error", "Post não encontrado para edição (ID: " + id + ").");
            return "redirect:/web/posts";
        }
    }
    //Editar post no banco de dados
    @PostMapping("/posts/update/{id}")
    public String updatePost(@PathVariable UUID id,
                             @Valid @ModelAttribute("post") BlogAppRecordDto postDto,
                             BindingResult bindingResult,
                             RedirectAttributes attributes,
                             Model model) {
        //Se estiver errado mostrar mensagem de erro e manter os dados
        if (bindingResult.hasErrors()) {
            model.addAttribute("post", postDto);
            model.addAttribute("postId", id);
            blogAppPostService.getBlogAppPostById(id)
                    .ifPresent(orig -> model.addAttribute("postDate", orig.getData()));
            return "editPostForm";
        }
        //Pegar o post por ID e se não existir mostrar pop up
        Optional<BlogAppPost> originalPostOptional = blogAppPostService.getBlogAppPostById(id);

        if (originalPostOptional.isEmpty()) {
            attributes.addFlashAttribute("error", "Post não encontrado para atualização (ID: " + id + ").");
            return "redirect:/web/posts";
        }
        //Tentar editar o post com os informações coletados, se der certo sucesso, se der errado erro
        BlogAppPost originalPost = originalPostOptional.get();
        try {
            originalPost.setTitulo(postDto.titulo());
            originalPost.setAutor(postDto.autor());
            originalPost.setTexto(postDto.texto());

            blogAppPostService.updateBlogAppPost(originalPost);
            attributes.addFlashAttribute("message", "Post atualizado com sucesso!");
            return "redirect:/web/posts";

        } catch (Exception e) {
            log.error("Error updating post {}", id, e);
            attributes.addFlashAttribute("error", "Erro ao atualizar o post.");
            model.addAttribute("post", postDto);
            model.addAttribute("postId", id);
            model.addAttribute("postDate", originalPost.getData());
            return "editPostForm";
        }
    }

    //Deleção de posts
    @PostMapping("/posts/delete/{id}")
    public String deletePost(@PathVariable UUID id, RedirectAttributes attributes) {
        //Deletar comentários do posts
        try {
            List<PostComentario> commentsToDelete = blogAppPostServiceComentarios.getComentariosByPostId(id);
            if (!commentsToDelete.isEmpty()) {
                log.info("Deleting {} comments for post {}", commentsToDelete.size(), id);
                for (PostComentario comment : commentsToDelete) {
                    blogAppPostServiceComentarios.deletePostComentario(comment.getId());
                }
            }
        } catch (Exception e) {
            log.error("Erro ao deletar comentários para o post {}", id, e);
            attributes.addFlashAttribute("error", "Erro ao deletar comentários associados ao post. O post não foi deletado.");
            return "redirect:/web/posts";
        }
        //Pegar post da database por ID
        Optional<BlogAppPost> postOptional = blogAppPostService.getBlogAppPostById(id);

        if (postOptional.isEmpty()) {
            log.warn("Tentativa de deletar post {} que não foi encontrado.", id);
            return "redirect:/web/posts";
        }
        //Tentar deletar
        try {
            blogAppPostService.deleteBlogAppPost(id);
            attributes.addFlashAttribute("message", "Post e seus comentários deletados com sucesso!");
            log.info("Post {} deletado com sucesso", id);
        } catch (Exception e) {
            log.error("Erro ao deletar o post {}", id, e);
            attributes.addFlashAttribute("error", "Erro ao deletar o post.");
            return "redirect:/web/posts";
        }
        return "redirect:/web/posts";
    }
}