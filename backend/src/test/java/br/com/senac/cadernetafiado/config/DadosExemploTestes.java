package br.com.senac.cadernetafiado.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.senac.cadernetafiado.model.Usuario;
import br.com.senac.cadernetafiado.repository.FiadoRepository;
import br.com.senac.cadernetafiado.repository.UsuarioRepository;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite::memory:")
class DadosExemploTestes {
    @Autowired
    private DadosExemplo dadosExemplo;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FiadoRepository fiadoRepository;

    @Test
    void criaUsuarioDemoComFiadosAoIniciar() {
        Usuario dono = usuarioRepository.buscarPorEmail(DadosExemplo.EMAIL_DEMO).orElseThrow();

        assertThat(new BCryptPasswordEncoder().matches(DadosExemplo.SENHA_DEMO, dono.getSenha())).isTrue();
        assertThat(fiadoRepository.listarPorUsuario(dono.getId())).hasSize(5);

        assertThat(fiadoRepository.somarTotalDevido(dono.getId())).isEqualByComparingTo("106.40");
    }

    @Test
    void naoDuplicaQuandoRodaDeNovo() {
        dadosExemplo.run();

        assertThat(usuarioRepository.count()).isEqualTo(1);
        assertThat(fiadoRepository.count()).isEqualTo(5);
    }
}
