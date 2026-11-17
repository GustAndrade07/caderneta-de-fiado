package br.com.senac.cadernetafiado.dto;

import java.math.BigDecimal;

/** Resposta de GET /fiados/total. */
public record TotalDevidoResponse(BigDecimal totalDevido) {
}
