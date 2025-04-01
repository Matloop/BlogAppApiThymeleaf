package com.api.BlogAppApi.dtos;

import jakarta.validation.constraints.NotBlank;

public record BlogAppRecordDto(@NotBlank String autor, @NotBlank String titulo, @NotBlank String texto) {

}
