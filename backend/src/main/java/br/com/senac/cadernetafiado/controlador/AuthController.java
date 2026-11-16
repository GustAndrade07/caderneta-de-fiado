package br.com.senac.cadernetafiado.controlador;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.senac.cadernetafiado.dto.CadastroUsuarioRequisicao;
import br.com.senac.cadernetafiado.dto.LoginRequisicao;
import br.com.senac.cadernetafiado.dto.TokenResposta;
import br.com.senac.cadernetafiado.dto.UsuarioResposta;
import br.com.senac.cadernetafiado.servico.AutenticacaoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AutenticacaoService autenticacaoService;

    public AuthController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResposta> registrar(@Valid @RequestBody CadastroUsuarioRequisicao requisicao) {
        return ResponseEntity.status(HttpStatus.CREATED).body(autenticacaoService.registrar(requisicao));
    }

    @PostMapping("/login")
    public TokenResposta login(@Valid @RequestBody LoginRequisicao requisicao) {
        return autenticacaoService.autenticar(requisicao);
    }
}
