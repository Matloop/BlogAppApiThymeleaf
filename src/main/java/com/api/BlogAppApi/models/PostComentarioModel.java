package com.api.BlogAppApi.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank; // Import validation
import jakarta.validation.constraints.NotNull;  // Import validation

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="TB-POSTCOMENTARIO") // Consider TB_POST_COMENTARIO if preferred
public class PostComentarioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Consider GenerationType.UUID or @GenericGenerator
    private UUID id;

    // No need to validate date if always set by server
    @Column(nullable = false) // Ensure DB enforces non-null
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDateTime date;

    @NotBlank(message = "Comment cannot be blank") // Add validation
    @Lob
    @Column(columnDefinition = "text", nullable = false) // Ensure DB enforces non-null
    private String comentario;

    @NotNull // The associated post should not be null
    @ManyToOne(fetch = FetchType.LAZY) // Lazy fetch is often better
    @JoinColumn(name = "post_model_id") // Explicitly define FK column name (optional but good practice)
    private BlogAppPostModel postModel;

    // --- Getters and Setters ---
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public BlogAppPostModel getPostModel() {
        return postModel;
    }

    public void setPostModel(BlogAppPostModel postModel) {
        this.postModel = postModel;
    }
}