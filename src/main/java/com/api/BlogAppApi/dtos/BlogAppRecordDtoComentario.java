package com.api.BlogAppApi.dtos;

import jakarta.validation.constraints.NotBlank; // Adicionado para validação

// DTO para receber e enviar dados de comentários para/de formulários
public record BlogAppRecordDtoComentario (
        @NotBlank(message = "O comentário não pode estar vazio.") // Adiciona validação básica
        String comentario
){
}