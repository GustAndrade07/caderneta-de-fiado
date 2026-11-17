package br.com.senac.cadernetafiado.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private static final Logger registro = LoggerFactory.getLogger(JwtService.class);

    private static final int MINIMO_DE_CARACTERES = 32;

    private final SecretKey chave;
    private final long expiracaoHoras;

    @Autowired
    public JwtService(@Value("${app.jwt.segredo:}") String segredo,
            @Value("${app.jwt.expiracao-horas:24}") long expiracaoHoras,
            @Value("${spring.profiles.active:}") String perfisAtivos) {
        this(segredo, expiracaoHoras, perfisAtivos != null && perfisAtivos.contains("prod"));
    }

    JwtService(String segredo, long expiracaoHoras, boolean producao) {
        this.expiracaoHoras = expiracaoHoras;
        this.chave = montarChave(segredo, producao);
    }

    private static SecretKey montarChave(String segredo, boolean producao) {
        if (segredo == null || segredo.isBlank()) {
            if (producao) {
                throw new IllegalStateException(
                        "Defina a variável de ambiente JWT_SECRET antes de subir em produção.");
            }
            registro.warn("JWT_SECRET não definida: subindo com chave aleatória de desenvolvimento. "
                    + "Os tokens só valem até a aplicação reiniciar.");
            return Jwts.SIG.HS256.key().build();
        }

        if (segredo.length() < MINIMO_DE_CARACTERES) {
            throw new IllegalStateException("A chave do JWT precisa de pelo menos "
                    + MINIMO_DE_CARACTERES + " caracteres (HS256). A atual tem " + segredo.length() + ".");
        }

        return Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
    }

    public String gerarToken(String email) {
        Instant agora = Instant.now();

        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(expiracaoHoras, ChronoUnit.HOURS)))
                .signWith(chave)
                .compact();
    }

    public Optional<String> lerEmail(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        try {
            String email = Jwts.parser()
                    .verifyWith(chave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();

            return Optional.ofNullable(email);
        } catch (JwtException | IllegalArgumentException erro) {
            registro.debug("Token recusado ({})", erro.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    public boolean tokenValido(String token) {
        return lerEmail(token).isPresent();
    }
}
