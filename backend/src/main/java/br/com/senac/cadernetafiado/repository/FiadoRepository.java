package br.com.senac.cadernetafiado.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.senac.cadernetafiado.model.Fiado;

public interface FiadoRepository extends JpaRepository<Fiado, Long> {
    @Query("SELECT f FROM Fiado f WHERE f.usuario.id = :usuarioId ORDER BY f.data DESC")
    List<Fiado> listarPorUsuario(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COALESCE(SUM(f.valor), 0) FROM Fiado f WHERE f.usuario.id = :usuarioId AND f.pago = false")
    BigDecimal somarTotalDevido(@Param("usuarioId") Long usuarioId);
}
