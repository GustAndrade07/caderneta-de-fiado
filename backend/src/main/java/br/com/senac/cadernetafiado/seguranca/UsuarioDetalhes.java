package br.com.senac.cadernetafiado.seguranca;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.senac.cadernetafiado.modelo.Usuario;

/**
 * Embrulha o {@link Usuario} no formato que o Spring Security entende.
 *
 * É isto que chega no controller com {@code @AuthenticationPrincipal}, então é
 * daqui que o Pedro e o Ryan tiram o dono da requisição — nunca do corpo ou da
 * URL, senão um usuário consegue ler o fiado do outro.
 */
public class UsuarioDetalhes implements UserDetails {

    private final transient Usuario usuario;

    public UsuarioDetalhes(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    /** O projeto não tem perfis: todo usuário autenticado tem os mesmos direitos. */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return usuario.getSenha();
    }

    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
