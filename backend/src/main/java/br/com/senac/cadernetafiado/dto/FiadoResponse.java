package br.com.senac.cadernetafiado.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.senac.cadernetafiado.model.Fiado;

public record FiadoResponse(
        Long id,
        String nomeCliente,
        BigDecimal valor,
        LocalDate data,
        String descricao,
        boolean pago) {
    public static FiadoResponse de(Fiado fiado) {
        return new FiadoResponse(fiado.getId(), fiado.getNomeCliente(), fiado.getValor(),
                fiado.getData(), fiado.getDescricao(), fiado.isPago());
    }
}
