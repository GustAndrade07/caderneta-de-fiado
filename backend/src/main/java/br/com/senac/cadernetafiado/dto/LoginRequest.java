package br.com.senac.cadernetafiado.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Use o formato nome@dominio.com (ex.: maria@exemplo.com).")
        @Pattern(regexp = "^[^\\s@]+@[^\\s@.]+(\\.[^\\s@.]+)+$",
                message = "Use o formato nome@dominio.com (ex.: maria@exemplo.com).") String email,
        @NotBlank(message = "A senha é obrigatória") String senha) {
}
