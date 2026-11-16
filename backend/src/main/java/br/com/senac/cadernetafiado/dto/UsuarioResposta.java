package br.com.senac.cadernetafiado.dto;

import br.com.senac.cadernetafiado.modelo.Usuario;

public record UsuarioResposta(Long id, String nome, String email) {

    public static UsuarioResposta de(Usuario usuario) {
        return new UsuarioResposta(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
