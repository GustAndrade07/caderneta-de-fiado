package br.com.senac.cadernetafiado.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.senac.cadernetafiado.dto.FiadoRequest;
import br.com.senac.cadernetafiado.dto.FiadoResponse;
import br.com.senac.cadernetafiado.modelo.Fiado;
import br.com.senac.cadernetafiado.modelo.Usuario;
import br.com.senac.cadernetafiado.repositorio.FiadoRepositorio;

/**
 * Regras de negócio do fiado. Toda operação recebe o usuário autenticado e só
 * enxerga fiados dele: o de outra pessoa responde "não encontrado", igual a um
 * id que não existe, para não revelar que o registro existe.
 */
@Service
public class FiadoService {

    private final FiadoRepositorio fiadoRepositorio;

    public FiadoService(FiadoRepositorio fiadoRepositorio) {
        this.fiadoRepositorio = fiadoRepositorio;
    }

    @Transactional
    public FiadoResponse criar(FiadoRequest dados, Usuario usuario) {
        // Novo fiado sempre começa não pago (pago = false por padrão na entidade)
        Fiado fiado = new Fiado(dados.nomeCliente(), dados.valor(), dados.data(), dados.descricao(), usuario);
        return FiadoResponse.de(fiadoRepositorio.save(fiado));
    }

    @Transactional(readOnly = true)
    public List<FiadoResponse> listar(Long usuarioId) {
        return fiadoRepositorio.listarPorUsuario(usuarioId).stream().map(FiadoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public FiadoResponse buscarPorId(Long id, Long usuarioId) {
        return FiadoResponse.de(buscarDoUsuario(id, usuarioId));
    }

    @Transactional
    public FiadoResponse atualizar(Long id, FiadoRequest dados, Long usuarioId) {
        Fiado fiado = buscarDoUsuario(id, usuarioId);

        // Dono e "pago" ficam intocados: só os campos editáveis mudam
        fiado.setNomeCliente(dados.nomeCliente());
        fiado.setValor(dados.valor());
        fiado.setData(dados.data());
        fiado.setDescricao(dados.descricao());

        return FiadoResponse.de(fiadoRepositorio.save(fiado));
    }

    @Transactional
    public void excluir(Long id, Long usuarioId) {
        fiadoRepositorio.delete(buscarDoUsuario(id, usuarioId));
    }

    @Transactional
    public FiadoResponse marcarComoPago(Long id, Long usuarioId) {
        Fiado fiado = buscarDoUsuario(id, usuarioId);
        fiado.setPago(true); // só o estado muda; o valor da dívida continua o mesmo
        return FiadoResponse.de(fiadoRepositorio.save(fiado));
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularTotal(Long usuarioId) {
        return fiadoRepositorio.somarTotalDevido(usuarioId);
    }

    private Fiado buscarDoUsuario(Long id, Long usuarioId) {
        return fiadoRepositorio.findById(id)
                .filter(fiado -> fiado.getUsuario().getId().equals(usuarioId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fiado não encontrado"));
    }
}
