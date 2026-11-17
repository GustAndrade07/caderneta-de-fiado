package br.com.senac.cadernetafiado.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.senac.cadernetafiado.dto.UsuarioResponse;
import br.com.senac.cadernetafiado.security.UsuarioDetalhes;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    @GetMapping("/me")
    public UsuarioResponse meuPerfil(@AuthenticationPrincipal UsuarioDetalhes usuarioDetalhes) {
        return UsuarioResponse.de(usuarioDetalhes.getUsuario());
    }
}
