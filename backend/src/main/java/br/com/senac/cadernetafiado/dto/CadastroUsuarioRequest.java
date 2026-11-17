package br.com.senac.cadernetafiado.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastroUsuarioRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Use o formato nome@dominio.com (ex.: maria@exemplo.com).")
        @Pattern(regexp = "^[^\\s@]+@[^\\s@.]+(\\.[^\\s@.]+)+$",
                message = "Use o formato nome@dominio.com (ex.: maria@exemplo.com).")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String senha) {
}