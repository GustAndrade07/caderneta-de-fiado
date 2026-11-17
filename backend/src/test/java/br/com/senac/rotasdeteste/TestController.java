package br.com.senac.rotasdeteste;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.senac.cadernetafiado.security.UsuarioDetalhes;

@RestController
public class TestController {
    @GetMapping("/teste/protegido")
    String protegido(@AuthenticationPrincipal UsuarioDetalhes logado) {
        return logado.getUsuario().getEmail() + ":" + logado.getUsuario().getId();
    }

    @GetMapping("/teste/rota-nova")
    String rotaNova() {
        return "nova";
    }

    @GetMapping("/auth/teste")
    String aberta() {
        return "aberta";
    }
}
