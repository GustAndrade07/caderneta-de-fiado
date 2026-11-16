package br.com.senac.cadernetafiado.controlador;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.senac.cadernetafiado.dto.UsuarioResposta;
import br.com.senac.cadernetafiado.seguranca.UsuarioDetalhes;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @GetMapping("/me")
    public UsuarioResposta meuPerfil(@AuthenticationPrincipal UsuarioDetalhes usuarioDetalhes) {
        return UsuarioResposta.de(usuarioDetalhes.getUsuario());
    }
}
