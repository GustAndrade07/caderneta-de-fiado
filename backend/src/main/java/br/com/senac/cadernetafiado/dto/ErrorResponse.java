package br.com.senac.cadernetafiado.dto;

public record ErrorResponse(int status, String erro, String mensagem) {
}
