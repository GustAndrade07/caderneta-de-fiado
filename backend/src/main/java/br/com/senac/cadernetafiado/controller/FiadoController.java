package br.com.senac.cadernetafiado.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.senac.cadernetafiado.dto.FiadoRequest;
import br.com.senac.cadernetafiado.dto.FiadoResponse;
import br.com.senac.cadernetafiado.dto.TotalDevidoResponse;
import br.com.senac.cadernetafiado.seguranca.UsuarioDetalhes;
import br.com.senac.cadernetafiado.service.FiadoService;
import jakarta.validation.Valid;

/** Camada HTTP do fiado: recebe, valida, delega ao service e responde. Sem regra de negócio. */
@RestController
@RequestMapping("/fiados")
public class FiadoController {

    private final FiadoService servico;

    public FiadoController(FiadoService servico) {
        this.servico = servico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FiadoResponse criar(@Valid @RequestBody FiadoRequest dados, @AuthenticationPrincipal UsuarioDetalhes logado) {
        return servico.criar(dados, logado.getUsuario());
    }

    @GetMapping
    public List<FiadoResponse> listar(@AuthenticationPrincipal UsuarioDetalhes logado) {
        return servico.listar(logado.getUsuario().getId());
    }

    @GetMapping("/total")
    public TotalDevidoResponse total(@AuthenticationPrincipal UsuarioDetalhes logado) {
        return new TotalDevidoResponse(servico.calcularTotal(logado.getUsuario().getId()));
    }

    @GetMapping("/{id}")
    public FiadoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal UsuarioDetalhes logado) {
        return servico.buscarPorId(id, logado.getUsuario().getId());
    }

    @PutMapping("/{id}")
    public FiadoResponse atualizar(@PathVariable Long id, @Valid @RequestBody FiadoRequest dados,
            @AuthenticationPrincipal UsuarioDetalhes logado) {
        return servico.atualizar(id, dados, logado.getUsuario().getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @AuthenticationPrincipal UsuarioDetalhes logado) {
        servico.excluir(id, logado.getUsuario().getId());
    }

    @PatchMapping("/{id}/pagar")
    public FiadoResponse pagar(@PathVariable Long id, @AuthenticationPrincipal UsuarioDetalhes logado) {
        return servico.marcarComoPago(id, logado.getUsuario().getId());
    }
}
