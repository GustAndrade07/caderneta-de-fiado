package br.com.senac.cadernetafiado.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.senac.cadernetafiado.model.Usuario;

@DataJpaTest(properties = "spring.datasource.url=jdbc:sqlite::memory:")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsuarioRepositoryTestes {
    @Autowired
    private UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder codificador = new BCryptPasswordEncoder();

    @Test
    void salvaEBuscaUsuarioPeloEmail() {
        String hash = codificador.encode("senha123");
        usuarioRepository.save(new Usuario("Maria", "maria@email.com", hash));

        Optional<Usuario> encontrado = usuarioRepository.buscarPorEmail("maria@email.com");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getId()).isNotNull();
        assertThat(encontrado.get().getNome()).isEqualTo("Maria");

        assertThat(encontrado.get().getSenha()).isNotEqualTo("senha123");
        assertThat(codificador.matches("senha123", encontrado.get().getSenha())).isTrue();
    }

    @Test
    void buscarPorEmailInexistenteRetornaVazio() {
        assertThat(usuarioRepository.buscarPorEmail("ninguem@email.com")).isEmpty();
    }

    @Test
    void verificaSeEmailJaExiste() {
        usuarioRepository.save(new Usuario("João", "joao@email.com", codificador.encode("abc123")));

        assertThat(usuarioRepository.existePorEmail("joao@email.com")).isTrue();
        assertThat(usuarioRepository.existePorEmail("outro@email.com")).isFalse();
    }
}
