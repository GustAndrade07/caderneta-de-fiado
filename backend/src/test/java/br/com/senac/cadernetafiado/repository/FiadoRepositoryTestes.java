package br.com.senac.cadernetafiado.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import br.com.senac.cadernetafiado.model.Fiado;
import br.com.senac.cadernetafiado.model.Usuario;

@DataJpaTest(properties = "spring.datasource.url=jdbc:sqlite::memory:")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FiadoRepositoryTestes {
    @Autowired
    private FiadoRepository fiadoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario ana;
    private Usuario bruno;

    @BeforeEach
    void prepararUsuarios() {
        ana = usuarioRepository.save(new Usuario("Ana", "ana@email.com", "hash-qualquer"));
        bruno = usuarioRepository.save(new Usuario("Bruno", "bruno@email.com", "hash-qualquer"));
    }

    @Test
    void salvaFiadoComoNaoPagoPorPadrao() {
        Fiado salvo = fiadoRepository.save(
                new Fiado("Seu Zé", new BigDecimal("12.50"), LocalDate.of(2026, 11, 10), "Pão e leite", ana));

        Fiado buscado = fiadoRepository.findById(salvo.getId()).orElseThrow();

        assertThat(buscado.getNomeCliente()).isEqualTo("Seu Zé");
        assertThat(buscado.getValor()).isEqualByComparingTo("12.50");
        assertThat(buscado.isPago()).isFalse();
        assertThat(buscado.getUsuario().getId()).isEqualTo(ana.getId());
    }

    @Test
    void listaSomenteOsFiadosDoUsuario() {
        fiadoRepository.save(new Fiado("Seu Zé", new BigDecimal("10.00"), LocalDate.of(2026, 11, 1), null, ana));
        fiadoRepository.save(new Fiado("Dona Rosa", new BigDecimal("20.00"), LocalDate.of(2026, 11, 5), null, ana));
        fiadoRepository.save(new Fiado("Carlos", new BigDecimal("99.00"), LocalDate.of(2026, 11, 3), null, bruno));

        List<Fiado> fiadosDaAna = fiadoRepository.listarPorUsuario(ana.getId());

        assertThat(fiadosDaAna).hasSize(2);

        assertThat(fiadosDaAna.get(0).getNomeCliente()).isEqualTo("Dona Rosa");
        assertThat(fiadosDaAna.get(1).getNomeCliente()).isEqualTo("Seu Zé");
    }

    @Test
    void somaApenasFiadosNaoPagosDoUsuario() {
        fiadoRepository.save(new Fiado("Seu Zé", new BigDecimal("10.50"), LocalDate.of(2026, 11, 1), null, ana));
        fiadoRepository.save(new Fiado("Dona Rosa", new BigDecimal("20.25"), LocalDate.of(2026, 11, 2), null, ana));

        Fiado jaPago = new Fiado("Pedro", new BigDecimal("100.00"), LocalDate.of(2026, 11, 3), null, ana);
        jaPago.setPago(true);
        fiadoRepository.save(jaPago);

        fiadoRepository.save(new Fiado("Carlos", new BigDecimal("50.00"), LocalDate.of(2026, 11, 4), null, bruno));

        assertThat(fiadoRepository.somarTotalDevido(ana.getId())).isEqualByComparingTo("30.75");
    }

    @Test
    void totalDevidoEZeroQuandoNaoHaFiados() {
        assertThat(fiadoRepository.somarTotalDevido(ana.getId())).isEqualByComparingTo("0");
    }
}
