package com.api.BlogAppApi.controllers;

import com.api.BlogAppApi.models.BlogAppPostModel;
import com.api.BlogAppApi.models.PostComentarioModel;
import com.api.BlogAppApi.services.BlogAppPostService;
import com.api.BlogAppApi.services.BlogAppPostServiceComentarios;
import org.springframework.ui.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException; // Import for DB exceptions
import org.springframework.stereotype.Controller;
// Import BindingResult if you plan to re-add validation later
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/web") // Handles both post details and comment actions now
public class BlogComentaryController {

    private static final Logger log = LoggerFactory.getLogger(BlogComentaryController.class);

    private final BlogAppPostServiceComentarios blogAppPostServiceComentarios;
    private final BlogAppPostService blogAppPostService;

    @Autowired
    public BlogComentaryController(BlogAppPostServiceComentarios blogAppPostServiceComentarios,
                                   BlogAppPostService blogAppPostService) {
        this.blogAppPostServiceComentarios = blogAppPostServiceComentarios;
        this.blogAppPostService = blogAppPostService;
    }

    // --- GET POST DETAILS (Handles rendering, including edit state) ---
    @GetMapping("/posts/{postId}")
    public ModelAndView getPostDetails(@PathVariable UUID postId, Model model, RedirectAttributes attributes) {
        // ... (getPostDetails method from previous step - no changes needed here for the update bug) ...
        log.info("Attempting to GET post details for ID: {}", postId);
        ModelAndView modelAndView = new ModelAndView();

        Optional<BlogAppPostModel> postOptional = blogAppPostService.getBlogAppPostById(postId);

        if (postOptional.isEmpty()) {
            log.warn("Post with ID {} NOT FOUND for details view.", postId);
            modelAndView.setViewName("redirect:/web/posts");
            attributes.addFlashAttribute("error", "Post com ID " + postId + " não encontrado.");
            return modelAndView;
        }

        BlogAppPostModel post = postOptional.get();
        log.info("Post FOUND for details view. Title: '{}'", post.getTitulo());

        List<PostComentarioModel> comments = blogAppPostServiceComentarios.getComentariosByPostId(postId);
        log.info("Found {} comments for post ID: {}", comments.size(), postId);

        modelAndView.addObject("post", post);
        modelAndView.addObject("comments", comments);

        if (!model.containsAttribute("newComment")) {
            modelAndView.addObject("newComment", new PostComentarioModel());
        }
        if (!model.containsAttribute("commentToEdit")) {
            modelAndView.addObject("commentToEdit", null);
        } else {
            log.info("Rendering in edit mode for comment: {}", model.getAttribute("commentToEdit"));
        }


        modelAndView.setViewName("postDetails");
        log.info("Setting view name to 'postDetails'. Editing comment: {}", model.containsAttribute("commentToEdit"));
        return modelAndView;
    }

    // --- SHOW EDIT COMMENT FORM (No changes needed here for the update bug) ---
    @GetMapping("/posts/{postId}/comments/{commentId}/edit")
    public ModelAndView showEditCommentForm(@PathVariable UUID postId,
                                            @PathVariable UUID commentId,
                                            RedirectAttributes attributes) {
        // ... (showEditCommentForm method from previous step - no changes needed here) ...
        log.info("Attempting to show EDIT form for comment ID: {} on post ID: {}", commentId, postId);
        ModelAndView modelAndView = new ModelAndView();

        Optional<BlogAppPostModel> postOptional = blogAppPostService.getBlogAppPostById(postId);
        if (postOptional.isEmpty()) {
            log.warn("Cannot edit comment. Post with ID {} not found.", postId);
            attributes.addFlashAttribute("error", "Post não encontrado.");
            modelAndView.setViewName("redirect:/web/posts");
            return modelAndView;
        }
        BlogAppPostModel post = postOptional.get();

        Optional<PostComentarioModel> commentOptional = blogAppPostServiceComentarios.getPostComentarioById(commentId);
        if (commentOptional.isEmpty()) {
            log.warn("Comment edit failed. Comment ID {} not found.", commentId);
            attributes.addFlashAttribute("error", "Comentário a ser editado não encontrado.");
            modelAndView.setViewName("redirect:/web/posts/" + postId);
            return modelAndView;
        }
        PostComentarioModel commentToEdit = commentOptional.get();

        if (commentToEdit.getPostModel() == null || !commentToEdit.getPostModel().getId().equals(postId)) {
            log.warn("Comment edit forbidden. Comment ID {} does not belong to post ID {}.", commentId, postId);
            attributes.addFlashAttribute("error", "Não é possível editar este comentário (permissão negada).");
            modelAndView.setViewName("redirect:/web/posts/" + postId);
            return modelAndView;
        }

        List<PostComentarioModel> allComments = blogAppPostServiceComentarios.getComentariosByPostId(postId);

        modelAndView.addObject("post", post);
        modelAndView.addObject("comments", allComments);
        modelAndView.addObject("commentToEdit", commentToEdit);
        modelAndView.addObject("newComment", new PostComentarioModel());

        modelAndView.setViewName("postDetails");
        log.info("Rendering postDetails view in edit mode for comment ID: {}", commentId);
        return modelAndView;
    }


    // --- UPDATE COMMENT (Enhanced Logging and Error Handling) ---
    @PostMapping("/posts/{postId}/comments/{commentId}/update")
    public String updateComment(@PathVariable UUID postId,
                                @PathVariable UUID commentId,
                                @RequestParam("comentario") String updatedComentario, // Name matches form input
                                RedirectAttributes attributes) {

        log.info("Attempting to UPDATE comment ID: {} on post ID: {} with text: '{}'", commentId, postId, updatedComentario);

        // Optional: Basic check for blank text (since @Valid was removed)
        if (updatedComentario == null || updatedComentario.trim().isEmpty()) {
            log.warn("Comment update failed for comment ID: {}. Comment text cannot be empty.", commentId);
            attributes.addFlashAttribute("error", "O texto do comentário não pode ficar vazio.");
            // Redirect back to the edit form URL
            return "redirect:/web/posts/" + postId + "/comments/" + commentId + "/edit";
        }

        Optional<PostComentarioModel> commentOptional;
        try {
            // 1. Fetch the existing comment
            commentOptional = blogAppPostServiceComentarios.getPostComentarioById(commentId);
        } catch (Exception e) {
            log.error("Error fetching comment ID {} for update: {}", commentId, e.getMessage(), e);
            attributes.addFlashAttribute("error", "Erro ao buscar comentário para atualização.");
            return "redirect:/web/posts/" + postId;
        }


        if (commentOptional.isEmpty()) {
            log.warn("Comment update failed. Comment ID {} not found.", commentId);
            attributes.addFlashAttribute("error", "Comentário não encontrado para atualização.");
            return "redirect:/web/posts/" + postId;
        }

        PostComentarioModel existingComment = commentOptional.get();
        log.debug("Found comment to update: {}", existingComment); // Log the fetched comment details

        // 2. Security/Consistency Check (with null check for postModel)
        try {
            if (existingComment.getPostModel() == null) {
                log.error("Comment update forbidden. Comment ID {} has a null postModel association.", commentId);
                attributes.addFlashAttribute("error", "Erro interno: Associação do comentário com o post está faltando.");
                return "redirect:/web/posts/" + postId;
            }
            if (!existingComment.getPostModel().getId().equals(postId)) {
                log.warn("Comment update forbidden. Comment ID {} does not belong to post ID {}.", commentId, postId);
                attributes.addFlashAttribute("error", "Não é possível atualizar este comentário (permissão negada).");
                return "redirect:/web/posts/" + postId;
            }
        } catch (NullPointerException npe) {
            log.error("NullPointerException during security check for comment update (ID: {}): {}", commentId, npe.getMessage(), npe);
            attributes.addFlashAttribute("error", "Erro inesperado ao verificar permissões do comentário.");
            return "redirect:/web/posts/" + postId;
        }


        // 3. Update the necessary fields
        log.debug("Updating comentario field for comment ID: {}", commentId);
        existingComment.setComentario(updatedComentario.trim());
        // Ensure other fields remain unchanged (date, postModel, id)

        // 4. Save the updated comment - Wrap in try-catch
        try {
            log.info("Attempting to save updated comment ID: {}", commentId);
            blogAppPostServiceComentarios.updatePostComentario(existingComment); // This calls repository.save()
            log.info("Comment ID: {} updated successfully on post ID: {}", commentId, postId);
            attributes.addFlashAttribute("message", "Comentário atualizado com sucesso!");
        } catch (DataAccessException dae) { // Catch database-related exceptions
            log.error("Database error updating comment ID {}: {}", commentId, dae.getMessage(), dae);
            attributes.addFlashAttribute("error", "Erro no banco de dados ao atualizar o comentário.");
            // Redirect back to edit form might be better so user doesn't lose context
            return "redirect:/web/posts/" + postId + "/comments/" + commentId + "/edit";
        } catch (Exception e) { // Catch any other unexpected exceptions
            log.error("Unexpected error updating comment ID {}: {}", commentId, e.getMessage(), e);
            attributes.addFlashAttribute("error", "Erro inesperado ao atualizar o comentário.");
            return "redirect:/web/posts/" + postId + "/comments/" + commentId + "/edit";
        }

        // 5. Redirect back to the post details page on success
        log.debug("Redirecting to post details page for post ID: {}", postId);
        return "redirect:/web/posts/" + postId;
    }


    // --- SAVE COMMENT (Keep previous version without validation) ---
    @PostMapping("/posts/{postId}/comments")
    public String saveComment(@PathVariable UUID postId,
                              @ModelAttribute("newComment") PostComentarioModel comment,
                              RedirectAttributes attributes) {
        // ... (saveComment method as before) ...
        log.info("Attempting to SAVE comment for post ID: {}", postId);
        Optional<BlogAppPostModel> postOptional = blogAppPostService.getBlogAppPostById(postId);
        if (postOptional.isEmpty()) {
            log.warn("Cannot save comment. Post with ID {} not found.", postId);
            attributes.addFlashAttribute("error", "Não foi possível adicionar o comentário. Post não encontrado.");
            return "redirect:/web/posts";
        }
        try {
            comment.setPostModel(postOptional.get());
            comment.setDate(LocalDateTime.now());
            blogAppPostServiceComentarios.savePostComentario(comment);
            log.info("Comment saved successfully for post ID: {}. Comment ID: {}", postId, comment.getId());
            attributes.addFlashAttribute("message", "Comentário adicionado com sucesso!");
        } catch (Exception e) {
            log.error("Error saving comment for post ID {}: {}", postId, e.getMessage(), e);
            attributes.addFlashAttribute("error", "Erro ao salvar o comentário. Verifique se o campo não está vazio.");
        }
        return "redirect:/web/posts/" + postId;
    }

    // --- DELETE COMMENT (Keep previous version) ---
    @PostMapping("/posts/{postId}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable UUID postId,
                                @PathVariable UUID commentId,
                                RedirectAttributes attributes) {
        // ... (deleteComment method as before) ...
        log.info("Attempting to DELETE comment ID: {} from post ID: {}", commentId, postId);
        Optional<PostComentarioModel> commentOptional = blogAppPostServiceComentarios.getPostComentarioById(commentId);
        if (commentOptional.isEmpty()) {
            log.warn("Comment deletion failed. Comment ID {} not found.", commentId);
            attributes.addFlashAttribute("error", "Comentário não encontrado para exclusão.");
            return "redirect:/web/posts/" + postId;
        }
        PostComentarioModel comment = commentOptional.get();
        if (comment.getPostModel() == null || !comment.getPostModel().getId().equals(postId)) {
            log.warn("Comment deletion forbidden. Comment ID {} does not belong to post ID {} or post link is missing.", commentId, postId);
            attributes.addFlashAttribute("error", "Não é possível deletar este comentário (permissão negada).");
            return "redirect:/web/posts/" + postId;
        }
        try {
            blogAppPostServiceComentarios.deletePostComentario(commentId);
            log.info("Comment ID: {} deleted successfully from post ID: {}", commentId, postId);
            attributes.addFlashAttribute("message", "Comentário deletado com sucesso!");
        } catch (Exception e) {
            log.error("Error deleting comment ID {}: {}", commentId, e.getMessage(), e);
            attributes.addFlashAttribute("error", "Erro ao deletar o comentário.");
        }
        return "redirect:/web/posts/" + postId;
    }

} // End of Class