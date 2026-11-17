package br.com.senac.cadernetafiado.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.senac.cadernetafiado.dto.FiadoRequest;
import br.com.senac.cadernetafiado.dto.FiadoResponse;
import br.com.senac.cadernetafiado.exception.FiadoNaoEncontradoException;
import br.com.senac.cadernetafiado.model.Fiado;
import br.com.senac.cadernetafiado.model.Usuario;
import br.com.senac.cadernetafiado.repository.FiadoRepository;

@Service
public class FiadoService {
    private final FiadoRepository fiadoRepository;

    public FiadoService(FiadoRepository fiadoRepository) {
        this.fiadoRepository = fiadoRepository;
    }

    @Transactional
    public FiadoResponse criar(FiadoRequest dados, Usuario usuario) {
        Fiado fiado = new Fiado(dados.nomeCliente(), dados.valor(), dados.data(), dados.descricao(), usuario);
        return FiadoResponse.de(fiadoRepository.save(fiado));
    }

    @Transactional(readOnly = true)
    public List<FiadoResponse> listar(Long usuarioId) {
        return fiadoRepository.listarPorUsuario(usuarioId).stream().map(FiadoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public FiadoResponse buscarPorId(Long id, Long usuarioId) {
        return FiadoResponse.de(buscarDoUsuario(id, usuarioId));
    }

    @Transactional
    public FiadoResponse atualizar(Long id, FiadoRequest dados, Long usuarioId) {
        Fiado fiado = buscarDoUsuario(id, usuarioId);

        fiado.setNomeCliente(dados.nomeCliente());
        fiado.setValor(dados.valor());
        fiado.setData(dados.data());
        fiado.setDescricao(dados.descricao());

        return FiadoResponse.de(fiadoRepository.save(fiado));
    }

    @Transactional
    public void excluir(Long id, Long usuarioId) {
        fiadoRepository.delete(buscarDoUsuario(id, usuarioId));
    }

    @Transactional
    public FiadoResponse marcarComoPago(Long id, Long usuarioId) {
        Fiado fiado = buscarDoUsuario(id, usuarioId);
        fiado.setPago(true);
        return FiadoResponse.de(fiadoRepository.save(fiado));
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularTotal(Long usuarioId) {
        return fiadoRepository.somarTotalDevido(usuarioId);
    }

    private Fiado buscarDoUsuario(Long id, Long usuarioId) {
        return fiadoRepository.findById(id)
                .filter(fiado -> fiado.getUsuario().getId().equals(usuarioId))
                .orElseThrow(() -> new FiadoNaoEncontradoException());
    }
}
