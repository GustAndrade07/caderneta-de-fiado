package br.com.senac.cadernetafiado.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Dados que o app envia para criar ou editar um fiado.
 *
 * Não tem "pago" nem "usuario" de propósito: o dono vem do token e o
 * pagamento só muda pelo endpoint próprio. Campos extras no JSON são ignorados.
 */
public record FiadoRequest(
        @NotBlank(message = "O nome do cliente é obrigatório") String nomeCliente,
        @NotNull(message = "O valor é obrigatório") @Positive(message = "O valor deve ser maior que zero") BigDecimal valor,
        @NotNull(message = "A data é obrigatória") LocalDate data,
        String descricao) {
}
