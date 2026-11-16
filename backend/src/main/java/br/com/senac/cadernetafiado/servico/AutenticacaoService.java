package br.com.senac.cadernetafiado.servico;

import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.senac.cadernetafiado.dto.CadastroUsuarioRequisicao;
import br.com.senac.cadernetafiado.dto.LoginRequisicao;
import br.com.senac.cadernetafiado.dto.TokenResposta;
import br.com.senac.cadernetafiado.dto.UsuarioResposta;
import br.com.senac.cadernetafiado.excecao.EmailJaCadastradoException;
import br.com.senac.cadernetafiado.modelo.Usuario;
import br.com.senac.cadernetafiado.repositorio.UsuarioRepositorio;
import br.com.senac.cadernetafiado.seguranca.JwtService;

@Service
public class AutenticacaoService {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder codificadorDeSenha;
    private final AuthenticationManager gerenciadorDeAutenticacao;
    private final JwtService jwtService;

    public AutenticacaoService(UsuarioRepositorio usuarioRepositorio, PasswordEncoder codificadorDeSenha,
            AuthenticationManager gerenciadorDeAutenticacao, JwtService jwtService) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.codificadorDeSenha = codificadorDeSenha;
        this.gerenciadorDeAutenticacao = gerenciadorDeAutenticacao;
        this.jwtService = jwtService;
    }

    public UsuarioResposta registrar(CadastroUsuarioRequisicao requisicao) {
        String email = normalizarEmail(requisicao.email());
        if (usuarioRepositorio.existePorEmail(email)) {
            throw new EmailJaCadastradoException();
        }

        Usuario usuario = new Usuario(
                requisicao.nome().trim(),
                email,
                codificadorDeSenha.encode(requisicao.senha()));

        return UsuarioResposta.de(usuarioRepositorio.save(usuario));
    }

    public TokenResposta autenticar(LoginRequisicao requisicao) {
        String email = normalizarEmail(requisicao.email());
        gerenciadorDeAutenticacao.authenticate(
                new UsernamePasswordAuthenticationToken(email, requisicao.senha()));

        return new TokenResposta(jwtService.gerarToken(email), "Bearer");
    }

    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
