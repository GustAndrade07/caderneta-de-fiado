package br.com.senac.cadernetafiado.service;

import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.senac.cadernetafiado.dto.CadastroUsuarioRequest;
import br.com.senac.cadernetafiado.dto.LoginRequest;
import br.com.senac.cadernetafiado.dto.TokenResponse;
import br.com.senac.cadernetafiado.dto.UsuarioResponse;
import br.com.senac.cadernetafiado.exception.EmailJaCadastradoException;
import br.com.senac.cadernetafiado.model.Usuario;
import br.com.senac.cadernetafiado.repository.UsuarioRepository;
import br.com.senac.cadernetafiado.security.JwtService;

@Service
public class AutenticacaoService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder codificadorDeSenha;
    private final AuthenticationManager gerenciadorDeAutenticacao;
    private final JwtService jwtService;

    public AutenticacaoService(UsuarioRepository usuarioRepository, PasswordEncoder codificadorDeSenha,
            AuthenticationManager gerenciadorDeAutenticacao, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.codificadorDeSenha = codificadorDeSenha;
        this.gerenciadorDeAutenticacao = gerenciadorDeAutenticacao;
        this.jwtService = jwtService;
    }

    public UsuarioResponse registrar(CadastroUsuarioRequest requisicao) {
        String email = normalizarEmail(requisicao.email());
        if (usuarioRepository.existePorEmail(email)) {
            throw new EmailJaCadastradoException();
        }

        Usuario usuario = new Usuario(
                requisicao.nome().trim(),
                email,
                codificadorDeSenha.encode(requisicao.senha()));

        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    public TokenResponse autenticar(LoginRequest requisicao) {
        String email = normalizarEmail(requisicao.email());
        gerenciadorDeAutenticacao.authenticate(
                new UsernamePasswordAuthenticationToken(email, requisicao.senha()));

        return new TokenResponse(jwtService.gerarToken(email), "Bearer");
    }

    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
