package com.api.BlogAppApi.controllers;

import com.api.BlogAppApi.models.BlogAppPostModel;
import com.api.BlogAppApi.models.PostComentarioModel; // Import Comment Model
import com.api.BlogAppApi.services.BlogAppPostService;
import com.api.BlogAppApi.services.BlogAppPostServiceComentarios; // Import Comment Service Interface
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
import java.util.Collections; // Import Collections
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/web")
public class BlogAppControllerWeb {

    private static final Logger log = LoggerFactory.getLogger(BlogAppControllerWeb.class);

    private final BlogAppPostService blogAppPostService;
    private final BlogAppPostServiceComentarios blogAppPostServiceComentarios; // Inject Comment Service

    @Autowired
    // Update constructor to inject the comment service
    public BlogAppControllerWeb(BlogAppPostService blogAppPostService,
                                BlogAppPostServiceComentarios blogAppPostServiceComentarios) {
        this.blogAppPostService = blogAppPostService;
        this.blogAppPostServiceComentarios = blogAppPostServiceComentarios; // Assign injected service
    }


    // Other methods (getPosts, getPostForm, savePost, getEditPostForm, updatePost, deletePost) remain largely the same
    // Ensure they still exist and function correctly. Adding logs to them is helpful.
    // ... [ Rest of the methods from previous examples, potentially with logging added ] ...

    @GetMapping("/posts")
    public ModelAndView getPosts() {
        log.info("Fetching all posts for /web/posts");
        ModelAndView modelAndView = new ModelAndView("posts");
        List<BlogAppPostModel> posts = blogAppPostService.getAllBlogAppPosts();
        modelAndView.addObject("posts", posts);
        log.info("Found {} posts.", posts.size());
        return modelAndView;
    }

    @GetMapping(value = "/newpost")
    public String getPostForm(Model model) {
        log.info("Showing new post form");
        model.addAttribute("post", new BlogAppPostModel());
        return "newPostForm";
    }

    @PostMapping(value = "/newpost")
    public String savePost(@Valid @ModelAttribute("post") BlogAppPostModel post,
                           BindingResult bindingResult,
                           RedirectAttributes attributes, Model model) { // Added Model
        log.info("Attempting to SAVE new post. Title: '{}'", post.getTitulo());
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors found while saving new post. Returning to form.");
            // Need model to hold the invalid post data when returning to form
            model.addAttribute("post", post);
            return "newPostForm"; // Return to the form view
        }
        post.setData(LocalDateTime.now());
        try {
            blogAppPostService.addBlogAppPost(post);
            log.info("New post saved successfully. ID: {}", post.getId());
            attributes.addFlashAttribute("message", "Post criado com sucesso!");
        } catch (Exception e) {
            log.error("Error saving new post: {}", e.getMessage(), e);
            attributes.addFlashAttribute("error", "Erro ao salvar o post.");
        }
        return "redirect:/web/posts";
    }

    @GetMapping("/posts/edit/{id}")
    public String getEditPostForm(@PathVariable UUID id, Model model, RedirectAttributes attributes) {
        log.info("Attempting to show EDIT form for post ID: {}", id);
        Optional<BlogAppPostModel> postOptional = blogAppPostService.getBlogAppPostById(id);

        if (postOptional.isPresent()) {
            log.info("Post found for edit. Title: '{}'", postOptional.get().getTitulo());
            model.addAttribute("post", postOptional.get());
            return "editPostForm";
        } else {
            log.warn("Post not found for edit. ID: {}", id);
            attributes.addFlashAttribute("error", "Post não encontrado para edição (ID: " + id + ").");
            return "redirect:/web/posts";
        }
    }

    @PostMapping("/posts/update/{id}")
    public String updatePost(@PathVariable UUID id,
                             @Valid @ModelAttribute("post") BlogAppPostModel postFormData,
                             BindingResult bindingResult,
                             RedirectAttributes attributes,
                             Model model) {
        log.info("Attempting to UPDATE post with ID: {}", id);
        // Set ID here before validation check in case we return to form
        postFormData.setId(id);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors found while updating post ID: {}. Returning to edit form.", id);
            // Fetch original date if possible to display correctly on form
            blogAppPostService.getBlogAppPostById(id).ifPresent(orig -> postFormData.setData(orig.getData()));
            model.addAttribute("post", postFormData); // Keep user data with errors
            return "editPostForm";
        }

        Optional<BlogAppPostModel> originalPostOptional = blogAppPostService.getBlogAppPostById(id);

        if (originalPostOptional.isEmpty()) {
            log.warn("Post not found for update. ID: {}", id);
            attributes.addFlashAttribute("error", "Post não encontrado para atualização (ID: " + id + ").");
            return "redirect:/web/posts";
        }

        BlogAppPostModel originalPost = originalPostOptional.get();
        log.info("Original post found for update. Title: '{}'", originalPost.getTitulo());

        originalPost.setTitulo(postFormData.getTitulo());
        originalPost.setAutor(postFormData.getAutor());
        originalPost.setTexto(postFormData.getTexto());

        try {
            blogAppPostService.updateBlogAppPost(originalPost);
            log.info("Post updated successfully. ID: {}", id);
            attributes.addFlashAttribute("message", "Post atualizado com sucesso!");
            return "redirect:/web/posts/" + id; // Redirect back to post details
        } catch (Exception e) {
            log.error("Error updating post with ID: {}: {}", id, e.getMessage(), e);
            attributes.addFlashAttribute("error", "Erro ao atualizar o post.");
            postFormData.setData(originalPost.getData()); // Keep original date for display
            model.addAttribute("post", postFormData);
            return "editPostForm";
        }
    }

    @PostMapping("/posts/delete/{id}")
    public String deletePost(@PathVariable UUID id, RedirectAttributes attributes) {
        log.info("Attempting to DELETE post with ID: {}", id);

        // *** IMPORTANT: Delete Comments First (or configure CascadeType.REMOVE) ***
        // If comments are not automatically deleted via Cascade, do it manually:
        try {
            List<PostComentarioModel> commentsToDelete = blogAppPostServiceComentarios.getComentariosByPostId(id);
            if (!commentsToDelete.isEmpty()) {
                log.info("Deleting {} comments associated with post ID: {}", commentsToDelete.size(), id);
                for (PostComentarioModel comment : commentsToDelete) {
                    blogAppPostServiceComentarios.deletePostComentario(comment.getId());
                }
            }
        } catch (Exception e) {
            log.error("Error deleting associated comments for post ID: {}: {}", id, e.getMessage(), e);
            attributes.addFlashAttribute("error", "Erro ao deletar comentários associados ao post.");
            // Decide if you want to proceed with post deletion or stop here
            return "redirect:/web/posts/" + id; // Redirect back to details page on comment deletion error
        }
        // *** End Comment Deletion ***


        Optional<BlogAppPostModel> postOptional = blogAppPostService.getBlogAppPostById(id);

        if (postOptional.isEmpty()) {
            log.warn("Post not found for deletion. ID: {}", id);
            attributes.addFlashAttribute("error", "Post não encontrado para exclusão (ID: " + id + ").");
            // Should redirect to posts list as the post doesn't exist anyway
            return "redirect:/web/posts";
        }

        try {
            log.info("Deleting post. Title: '{}'", postOptional.get().getTitulo());
            blogAppPostService.deleteBlogAppPost(id);
            log.info("Post deleted successfully. ID: {}", id);
            attributes.addFlashAttribute("message", "Post e seus comentários deletados com sucesso!");
        } catch (Exception e) {
            log.error("Error deleting post with ID: {}: {}", id, e.getMessage(), e);
            attributes.addFlashAttribute("error", "Erro ao deletar o post.");
            // Redirect back to posts list as the details page won't exist
            return "redirect:/web/posts";
        }
        // Redirect to the main posts list after successful deletion
        return "redirect:/web/posts";
    }
}