package br.com.senac.cadernetafiado.erro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import br.com.senac.cadernetafiado.dto.ErroResposta;
import br.com.senac.cadernetafiado.excecao.EmailJaCadastradoException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger registro = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErroResposta> emailDuplicado(EmailJaCadastradoException erro) {
        return responder(HttpStatus.CONFLICT, "email_duplicado", erro.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> conflitoDeDados(DataIntegrityViolationException erro) {
        return responder(HttpStatus.CONFLICT, "conflito_de_dados", "Não foi possível salvar os dados informados");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResposta> credenciaisInvalidas(AuthenticationException erro) {
        return responder(HttpStatus.UNAUTHORIZED, "credenciais_invalidas", "E-mail ou senha inválidos");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResposta> acessoNegado(AccessDeniedException erro) {
        return responder(HttpStatus.FORBIDDEN, "acesso_negado", "Você não tem acesso a este recurso");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> dadosInvalidos(MethodArgumentNotValidException erro) {
        String mensagem = erro.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Dados inválidos");
        return responder(HttpStatus.BAD_REQUEST, "dados_invalidos", mensagem);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, ConstraintViolationException.class})
    public ResponseEntity<ErroResposta> requisicaoInvalida(Exception erro) {
        return responder(HttpStatus.BAD_REQUEST, "requisicao_invalida", "A requisição contém dados inválidos");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResposta> recursoNaoEncontrado(NoResourceFoundException erro) {
        return responder(HttpStatus.NOT_FOUND, "Not Found", "Recurso não encontrado");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> erroInterno(Exception erro) {
        registro.error("Erro inesperado ao processar a requisição ({})", erro.getClass().getSimpleName());
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "erro_interno",
                "Ocorreu um erro interno. Tente novamente");
    }

    private static ResponseEntity<ErroResposta> responder(HttpStatus status, String erro, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResposta(status.value(), erro, mensagem));
    }
}
