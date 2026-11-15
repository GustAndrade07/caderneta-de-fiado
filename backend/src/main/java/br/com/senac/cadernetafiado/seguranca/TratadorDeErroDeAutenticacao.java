package br.com.senac.cadernetafiado.seguranca;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Devolve 401 e 403 em JSON.
 *
 * Erro que acontece dentro do filtro não passa pelo {@code @ControllerAdvice},
 * por isso ele é tratado aqui. O formato precisa ser o mesmo que o do Ryan,
 * porque o app do João Pedro lê este corpo para deslogar o usuário.
 */
@Component
public class TratadorDeErroDeAutenticacao implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest requisicao, HttpServletResponse resposta,
            AuthenticationException erro) throws IOException {

        responder(resposta, HttpStatus.UNAUTHORIZED, "nao_autorizado", "Token ausente ou inválido");
    }

    @Override
    public void handle(HttpServletRequest requisicao, HttpServletResponse resposta,
            AccessDeniedException erro) throws IOException {

        responder(resposta, HttpStatus.FORBIDDEN, "acesso_negado", "Você não tem acesso a este recurso");
    }

    private void responder(HttpServletResponse resposta, HttpStatus situacao, String erro, String mensagem)
            throws IOException {

        resposta.setStatus(situacao.value());
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resposta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resposta.getWriter().write("{\"status\":%d,\"erro\":\"%s\",\"mensagem\":\"%s\"}"
                .formatted(situacao.value(), erro, mensagem));
    }
}
