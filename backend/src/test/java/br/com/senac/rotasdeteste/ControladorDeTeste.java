package br.com.senac.rotasdeteste;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.senac.cadernetafiado.seguranca.UsuarioDetalhes;

/**
 * Rotas que existem só nos testes de segurança, para provar o filtro sem
 * depender dos endpoints do Pedro e do Ryan.
 *
 * Fica fora do pacote {@code br.com.senac.cadernetafiado} de propósito: assim o
 * component scan não a encontra e ela não entra no contexto dos testes dos
 * outros. Quem a usa importa explicitamente com {@code @Import}.
 */
@RestController
public class ControladorDeTeste {

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
