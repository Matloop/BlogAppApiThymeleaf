package com.api.BlogAppApi.controllers;

import com.api.BlogAppApi.dtos.BlogAppRecordDtoComentario; // Importar DTO de comentário
import com.api.BlogAppApi.models.BlogAppPost;
import com.api.BlogAppApi.models.PostComentario;
import com.api.BlogAppApi.services.BlogAppPostService;
import com.api.BlogAppApi.services.BlogAppPostServiceComentarios;
import jakarta.validation.Valid; // Importar para validação do DTO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // Importar para resultados de validação
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/web") // Mantém o prefixo, agora lidando com detalhes de post e comentários
public class BlogComentaryController {

    private final BlogAppPostServiceComentarios blogAppPostServiceComentarios;
    private final BlogAppPostService blogAppPostService;

    @Autowired
    // Injeção das classes services
    public BlogComentaryController(BlogAppPostServiceComentarios blogAppPostServiceComentarios,
                                   BlogAppPostService blogAppPostService) {
        this.blogAppPostServiceComentarios = blogAppPostServiceComentarios;
        this.blogAppPostService = blogAppPostService;
    }

    /**
     * Retorna a view com os detalhes de um post específico e seus comentários.
     * Também prepara o modelo com DTOs para adicionar ou editar comentários.
     * @param postId O ID do post a ser exibido (da URL).
     * @param model O modelo para adicionar atributos para a view.
     * @param attributes Usado para mensagens flash se o post não for encontrado.
     * @return ModelAndView contendo os dados do post, comentários e DTOs para a view "postDetails".
     */
    @GetMapping("/posts/{postId}")
    public ModelAndView getPostDetails(@PathVariable UUID postId, Model model, RedirectAttributes attributes) {
        ModelAndView modelAndView = new ModelAndView();

        Optional<BlogAppPost> postOptional = blogAppPostService.getBlogAppPostById(postId);

        if (postOptional.isEmpty()) {
            modelAndView.setViewName("redirect:/web/posts"); // Redireciona para lista geral
            attributes.addFlashAttribute("error", "Post com ID " + postId + " não encontrado.");
            return modelAndView;
        }

        BlogAppPost post = postOptional.get();

        // Busca os comentários associados
        List<PostComentario> comments = blogAppPostServiceComentarios.getComentariosByPostId(postId);

        modelAndView.addObject("post", post);
        modelAndView.addObject("comments", comments);

        // Prepara DTO para o formulário de *novo* comentário, se não veio de um redirect com erro
        if (!model.containsAttribute("newCommentDto")) {
            modelAndView.addObject("newCommentDto", new BlogAppRecordDtoComentario(""));
        }

        // Prepara DTO para o formulário de *edição*, se não veio de um redirect com erro
        if (!model.containsAttribute("commentToEditDto")) {
            // Nenhum comentário está ativamente sendo editado nesta requisição inicial
            modelAndView.addObject("commentToEditDto", null); // Indica que não estamos no modo de edição
            modelAndView.addObject("editingCommentId", null); // Garante que ID de edição está nulo
        }

        modelAndView.setViewName("postDetails");
        return modelAndView;
    }

    /**
     * Prepara e exibe a view de detalhes do post com o formulário de edição para um comentário específico.
     * @param postId O ID do post pai.
     * @param commentId O ID do comentário a ser editado.
     * @param attributes Usado para mensagens flash em caso de erro.
     * @return ModelAndView configurado para exibir "postDetails" em modo de edição de comentário.
     */
    @GetMapping("/posts/{postId}/comments/{commentId}/edit")
    public ModelAndView showEditCommentForm(@PathVariable UUID postId,
                                            @PathVariable UUID commentId,
                                            RedirectAttributes attributes,
                                            Model model) { // Adicionado Model para verificar flash attributes
        ModelAndView modelAndView = new ModelAndView();

        Optional<BlogAppPost> postOptional = blogAppPostService.getBlogAppPostById(postId);
        if (postOptional.isEmpty()) {
            attributes.addFlashAttribute("error", "Post não encontrado.");
            modelAndView.setViewName("redirect:/web/posts");
            return modelAndView;
        }
        BlogAppPost post = postOptional.get();

        Optional<PostComentario> commentOptional = blogAppPostServiceComentarios.getPostComentarioById(commentId);
        if (commentOptional.isEmpty()) {
            attributes.addFlashAttribute("error", "Comentário a ser editado não encontrado.");
            modelAndView.setViewName("redirect:/web/posts/" + postId);
            return modelAndView;
        }
        PostComentario commentToEditEntity = commentOptional.get();

        // Validação de segurança: o comentário pertence a este post?
        if (commentToEditEntity.getPostModel() == null || !commentToEditEntity.getPostModel().getId().equals(postId)) {
            attributes.addFlashAttribute("error", "Não é possível editar este comentário (permissão negada).");
            modelAndView.setViewName("redirect:/web/posts/" + postId);
            return modelAndView;
        }

        // Adiciona os dados necessários à view
        modelAndView.addObject("post", post);
        modelAndView.addObject("comments", blogAppPostServiceComentarios.getComentariosByPostId(postId)); // Todos os comentários
        modelAndView.addObject("newCommentDto", new BlogAppRecordDtoComentario("")); // DTO para novo comentário

        // Prepara o DTO para edição
        // Se viemos de um redirect com erro de validação, usamos o DTO do flash attribute
        if (model.containsAttribute("commentToEditDto")) {
            // modelAndView já terá o "commentToEditDto" do flash attribute
            modelAndView.addObject("editingCommentId", commentId); // Garante que o ID está presente
        } else {
            // Caso contrário, cria um DTO a partir da entidade encontrada
            BlogAppRecordDtoComentario commentDto = new BlogAppRecordDtoComentario(commentToEditEntity.getComentario());
            modelAndView.addObject("commentToEditDto", commentDto);
            modelAndView.addObject("editingCommentId", commentId); // ID do comentário sendo editado
        }

        modelAndView.setViewName("postDetails"); // Usa a mesma view, mas com dados para edição
        return modelAndView;
    }


    /**
     * Atualiza um comentário existente no banco de dados.
     * Recebe os dados via DTO, valida, busca a entidade original, atualiza e salva.
     * @param postId O ID do post pai.
     * @param commentId O ID do comentário a ser atualizado.
     * @param commentDto O DTO contendo os dados atualizados do comentário.
     * @param bindingResult O resultado da validação do DTO.
     * @param attributes Usado para mensagens flash.
     * @return Uma string de redirecionamento para a página de detalhes do post ou para o formulário de edição em caso de erro.
     */
    @PostMapping("/posts/{postId}/comments/{commentId}/update")
    public String updateComment(@PathVariable UUID postId,
                                @PathVariable UUID commentId,
                                @Valid @ModelAttribute("commentToEditDto") BlogAppRecordDtoComentario commentDto, // Recebe DTO
                                BindingResult bindingResult, // Para validação
                                RedirectAttributes attributes) {

        // 1. Verifica erros de validação no DTO
        if (bindingResult.hasErrors()) {
            attributes.addFlashAttribute("error", "Erro ao atualizar: " + bindingResult.getFieldError().getDefaultMessage());
            // Adiciona o DTO inválido e o ID de edição aos flash attributes para repopular o formulário
            attributes.addFlashAttribute("commentToEditDto", commentDto);
            attributes.addFlashAttribute("editingCommentId", commentId);
            // Redireciona DE VOLTA para a URL que MOSTRA o formulário de edição
            return "redirect:/web/posts/" + postId + "/comments/" + commentId + "/edit";
        }

        Optional<PostComentario> commentOptional;
        try {
            // 2. Busca o comentário existente
            commentOptional = blogAppPostServiceComentarios.getPostComentarioById(commentId);
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Erro ao buscar comentário para atualização.");
            return "redirect:/web/posts/" + postId;
        }

        if (commentOptional.isEmpty()) {
            attributes.addFlashAttribute("error", "Comentário não encontrado para atualização.");
            return "redirect:/web/posts/" + postId;
        }

        PostComentario existingComment = commentOptional.get();

        // 3. Verificação de Segurança/Consistência
        try {
            if (existingComment.getPostModel() == null) {
                attributes.addFlashAttribute("error", "Erro interno: Associação do comentário com o post está faltando.");
                return "redirect:/web/posts/" + postId;
            }
            if (!existingComment.getPostModel().getId().equals(postId)) {
                attributes.addFlashAttribute("error", "Não é possível atualizar este comentário (permissão negada).");
                return "redirect:/web/posts/" + postId;
            }
        } catch (NullPointerException npe) {
            attributes.addFlashAttribute("error", "Erro inesperado ao verificar permissões do comentário.");
            return "redirect:/web/posts/" + postId;
        }

        // 4. Atualiza o campo da entidade com o dado do DTO
        existingComment.setComentario(commentDto.comentario().trim()); // Usa dado do DTO

        // 5. Salva o comentário atualizado
        try {
            blogAppPostServiceComentarios.updatePostComentario(existingComment);
            attributes.addFlashAttribute("message", "Comentário atualizado com sucesso!");
        } catch (DataAccessException dae) {
            attributes.addFlashAttribute("error", "Erro no banco de dados ao atualizar o comentário.");
            // Adiciona DTO e ID para repopular formulário
            attributes.addFlashAttribute("commentToEditDto", commentDto);
            attributes.addFlashAttribute("editingCommentId", commentId);
            return "redirect:/web/posts/" + postId + "/comments/" + commentId + "/edit";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Erro inesperado ao atualizar o comentário.");
            // Adiciona DTO e ID para repopular formulário
            attributes.addFlashAttribute("commentToEditDto", commentDto);
            attributes.addFlashAttribute("editingCommentId", commentId);
            return "redirect:/web/posts/" + postId + "/comments/" + commentId + "/edit";
        }

        // 6. Redireciona para a página de detalhes do post em caso de sucesso
        return "redirect:/web/posts/" + postId;
    }

    /**
     * Salva um novo comentário para um post específico.
     * Recebe os dados via DTO, valida, mapeia para a entidade e salva.
     * @param postId O ID do post ao qual o comentário pertence.
     * @param commentDto O DTO contendo os dados do novo comentário.
     * @param bindingResult O resultado da validação do DTO.
     * @param attributes Usado para mensagens flash.
     * @return Uma string de redirecionamento para a página de detalhes do post.
     */
    @PostMapping("/posts/{postId}/comments")
    public String saveComment(@PathVariable UUID postId,
                              @Valid @ModelAttribute("newCommentDto") BlogAppRecordDtoComentario commentDto, // Recebe DTO
                              BindingResult bindingResult, // Para validação
                              RedirectAttributes attributes) {

        // 1. Verifica erros de validação no DTO
        if (bindingResult.hasErrors()) {
            attributes.addFlashAttribute("error", "Erro ao salvar: " + bindingResult.getFieldError().getDefaultMessage());
            // Adiciona o DTO inválido de volta via flash attributes para repopular o form
            attributes.addFlashAttribute("newCommentDto", commentDto);
            return "redirect:/web/posts/" + postId; // Volta para detalhes do post
        }


        // 2. Busca o post pai
        Optional<BlogAppPost> postOptional = blogAppPostService.getBlogAppPostById(postId);
        if (postOptional.isEmpty()) {
            attributes.addFlashAttribute("error", "Não foi possível adicionar o comentário. Post não encontrado.");
            return "redirect:/web/posts"; // Redireciona para lista geral se post sumiu
        }

        // 3. Mapeia DTO para nova entidade Comentario
        PostComentario newComment = new PostComentario();
        newComment.setComentario(commentDto.comentario().trim()); // Pega do DTO
        newComment.setPostModel(postOptional.get()); // Associa ao post pai
        newComment.setDate(LocalDateTime.now()); // Define data de criação

        // 4. Salva a nova entidade
        try {
            blogAppPostServiceComentarios.savePostComentario(newComment);
            attributes.addFlashAttribute("message", "Comentário adicionado com sucesso!");
        } catch (DataAccessException dae) {
            attributes.addFlashAttribute("error", "Erro de banco de dados ao salvar o comentário.");
            attributes.addFlashAttribute("newCommentDto", commentDto); // Devolve DTO em caso de erro
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Erro ao salvar o comentário.");
            attributes.addFlashAttribute("newCommentDto", commentDto); // Devolve DTO em caso de erro
        }

        // 5. Redireciona para a página de detalhes do post
        return "redirect:/web/posts/" + postId;
    }

    /**
     * Deleta um comentário específico.
     * @param postId O ID do post pai (usado para redirecionamento e validação).
     * @param commentId O ID do comentário a ser deletado.
     * @param attributes Usado para mensagens flash.
     * @return Uma string de redirecionamento para a página de detalhes do post.
     */
    @PostMapping("/posts/{postId}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable UUID postId,
                                @PathVariable UUID commentId,
                                RedirectAttributes attributes) {

        // 1. Busca o comentário para garantir que pertence ao post correto (segurança)
        Optional<PostComentario> commentOptional = blogAppPostServiceComentarios.getPostComentarioById(commentId);
        if (commentOptional.isEmpty()) {
            // Não precisa de flash attribute se o comentário já sumiu
            return "redirect:/web/posts/" + postId;
        }
        PostComentario comment = commentOptional.get();

        // Validação de segurança
        if (comment.getPostModel() == null || !comment.getPostModel().getId().equals(postId)) {
            attributes.addFlashAttribute("error", "Não é possível deletar este comentário (permissão negada).");
            return "redirect:/web/posts/" + postId;
        }

        // 2. Tenta deletar o comentário
        try {
            blogAppPostServiceComentarios.deletePostComentario(commentId);
            attributes.addFlashAttribute("message", "Comentário deletado com sucesso!");
        } catch (DataAccessException dae){
            attributes.addFlashAttribute("error", "Erro de banco de dados ao deletar o comentário.");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Erro ao deletar o comentário.");
        }

        // 3. Redireciona para a página de detalhes do post
        return "redirect:/web/posts/" + postId;
    }

} // Fim da Classe BlogComentaryController