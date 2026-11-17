package br.com.senac.cadernetafiado.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FiadoRequest(
        @NotBlank(message = "O nome do cliente é obrigatório") String nomeCliente,
        @NotNull(message = "O valor é obrigatório") @Positive(message = "O valor deve ser maior que zero") BigDecimal valor,
        @NotNull(message = "A data é obrigatória") LocalDate data,
        String descricao) {
}
