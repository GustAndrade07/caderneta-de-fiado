package br.com.senac.cadernetafiado.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtFiltro extends OncePerRequestFilter {
    private static final Logger registro = LoggerFactory.getLogger(JwtFiltro.class);
    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioDetalhesService usuarioDetalhesService;

    public JwtFiltro(JwtService jwtService, UsuarioDetalhesService usuarioDetalhesService) {
        this.jwtService = jwtService;
        this.usuarioDetalhesService = usuarioDetalhesService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta,
            FilterChain corrente) throws ServletException, IOException {
        String cabecalho = requisicao.getHeader(HttpHeaders.AUTHORIZATION);

        if (cabecalho != null && cabecalho.startsWith(PREFIXO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = cabecalho.substring(PREFIXO.length());
            jwtService.lerEmail(token).ifPresent(email -> autenticar(email, requisicao));
        }

        corrente.doFilter(requisicao, resposta);
    }

    private void autenticar(String email, HttpServletRequest requisicao) {
        try {
            UserDetails detalhes = usuarioDetalhesService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken autenticacao = new UsernamePasswordAuthenticationToken(
                    detalhes, null, detalhes.getAuthorities());
            autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(requisicao));

            SecurityContextHolder.getContext().setAuthentication(autenticacao);
        } catch (UsernameNotFoundException erro) {
            registro.debug("Token de usuário que não existe mais");
        }
    }
}
