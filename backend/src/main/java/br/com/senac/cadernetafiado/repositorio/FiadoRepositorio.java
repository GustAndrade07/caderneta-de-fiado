package br.com.senac.cadernetafiado.repositorio;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.senac.cadernetafiado.modelo.Fiado;

/**
 * Acesso à tabela de fiados.
 */
public interface FiadoRepositorio extends JpaRepository<Fiado, Long> {

    // Todos os fiados de um usuário, do mais recente para o mais antigo
    @Query("SELECT f FROM Fiado f WHERE f.usuario.id = :usuarioId ORDER BY f.data DESC")
    List<Fiado> listarPorUsuario(@Param("usuarioId") Long usuarioId);

    // Soma dos fiados ainda não pagos. COALESCE devolve 0 quando não há nenhum.
    @Query("SELECT COALESCE(SUM(f.valor), 0) FROM Fiado f WHERE f.usuario.id = :usuarioId AND f.pago = false")
    BigDecimal somarTotalDevido(@Param("usuarioId") Long usuarioId);
}
