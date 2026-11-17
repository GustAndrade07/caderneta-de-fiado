package br.com.senac.cadernetafiado.dto;

import br.com.senac.cadernetafiado.model.Usuario;

public record UsuarioResponse(Long id, String nome, String email) {
    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
