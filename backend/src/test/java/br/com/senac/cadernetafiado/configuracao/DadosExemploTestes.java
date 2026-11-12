package br.com.senac.cadernetafiado.configuracao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.senac.cadernetafiado.modelo.Usuario;
import br.com.senac.cadernetafiado.repositorio.FiadoRepositorio;
import br.com.senac.cadernetafiado.repositorio.UsuarioRepositorio;

// Sobe a aplicação inteira com um SQLite em memória.
// Ao subir, o Spring já executa o DadosExemplo, então o banco começa preenchido.
@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite::memory:")
class DadosExemploTestes {

    @Autowired
    private DadosExemplo dadosExemplo;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private FiadoRepositorio fiadoRepositorio;

    @Test
    void criaUsuarioDemoComFiadosAoIniciar() {
        Usuario dono = usuarioRepositorio.buscarPorEmail(DadosExemplo.EMAIL_DEMO).orElseThrow();

        assertThat(new BCryptPasswordEncoder().matches(DadosExemplo.SENHA_DEMO, dono.getSenha())).isTrue();
        assertThat(fiadoRepositorio.listarPorUsuario(dono.getId())).hasSize(5);
        // 35,90 + 12,50 + 58,00 (os dois pagos ficam de fora)
        assertThat(fiadoRepositorio.somarTotalDevido(dono.getId())).isEqualByComparingTo("106.40");
    }

    @Test
    void naoDuplicaQuandoRodaDeNovo() {
        dadosExemplo.run();

        assertThat(usuarioRepositorio.count()).isEqualTo(1);
        assertThat(fiadoRepositorio.count()).isEqualTo(5);
    }
}
