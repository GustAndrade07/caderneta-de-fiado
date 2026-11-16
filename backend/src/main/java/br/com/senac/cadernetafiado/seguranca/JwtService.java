package br.com.senac.cadernetafiado.seguranca;

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

/**
 * Gera e confere o token JWT.
 *
 * O token tem três partes separadas por ponto: cabeçalho (o algoritmo),
 * corpo (o e-mail em "sub", a emissão e a expiração) e assinatura. Quem tem a
 * chave consegue conferir a assinatura; sem ela, não dá para forjar um token.
 * Por isso a chave fica só no back-end, em variável de ambiente.
 */
@Service
public class JwtService {

    private static final Logger registro = LoggerFactory.getLogger(JwtService.class);

    /** HS256 exige uma chave de pelo menos 256 bits, ou seja, 32 caracteres. */
    private static final int MINIMO_DE_CARACTERES = 32;

    private final SecretKey chave;
    private final long expiracaoHoras;

    @Autowired
    public JwtService(@Value("${app.jwt.segredo:}") String segredo,
            @Value("${app.jwt.expiracao-horas:24}") long expiracaoHoras,
            @Value("${spring.profiles.active:}") String perfisAtivos) {
        this(segredo, expiracaoHoras, perfisAtivos != null && perfisAtivos.contains("prod"));
    }

    // Usado pelos testes, que montam o serviço sem subir o Spring
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

    /** Cria o token do usuário. Só vai o e-mail: nada de senha ou dado pessoal aqui dentro. */
    public String gerarToken(String email) {
        Instant agora = Instant.now();

        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(expiracaoHoras, ChronoUnit.HOURS)))
                .signWith(chave)
                .compact();
    }

    /**
     * Devolve o e-mail de dentro do token, ou vazio se ele for inválido:
     * assinatura de outra chave, prazo vencido ou texto que nem é um JWT.
     */
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
            // Nunca registrar o token no log: ele é uma credencial
            registro.debug("Token recusado ({})", erro.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    public boolean tokenValido(String token) {
        return lerEmail(token).isPresent();
    }
}
