package br.com.senac.cadernetafiado.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.senac.cadernetafiado.dto.CadastroUsuarioRequest;
import br.com.senac.cadernetafiado.dto.LoginRequest;
import br.com.senac.cadernetafiado.dto.TokenResponse;
import br.com.senac.cadernetafiado.dto.UsuarioResponse;
import br.com.senac.cadernetafiado.service.AutenticacaoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AutenticacaoService autenticacaoService;

    public AuthController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody CadastroUsuarioRequest requisicao) {
        return ResponseEntity.status(HttpStatus.CREATED).body(autenticacaoService.registrar(requisicao));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest requisicao) {
        return autenticacaoService.autenticar(requisicao);
    }
}
