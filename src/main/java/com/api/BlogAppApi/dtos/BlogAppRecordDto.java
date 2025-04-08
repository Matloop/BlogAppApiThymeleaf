package com.api.BlogAppApi.dtos;

import jakarta.validation.constraints.NotBlank;

// DTO for receiving and sending post data to/from forms
public record BlogAppRecordDto(
        @NotBlank String autor,
        @NotBlank String titulo,
        @NotBlank String texto
) {
}