package br.com.senac.cadernetafiado.seguranca;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.senac.cadernetafiado.repositorio.UsuarioRepositorio;

/**
 * Busca o usuário pelo e-mail que veio dentro do token.
 * Usa o {@code buscarPorEmail} que já existe no repositório.
 */
@Service
public class UsuarioDetalhesService implements UserDetailsService {

    private final UsuarioRepositorio usuarioRepositorio;

    public UsuarioDetalhesService(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return usuarioRepositorio.buscarPorEmail(email)
                .map(UsuarioDetalhes::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
