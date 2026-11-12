package br.com.senac.cadernetafiado.repositorio;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.senac.cadernetafiado.modelo.Usuario;

// @DataJpaTest sobe só a parte de banco do Spring. Cada teste roda numa
// transação desfeita no final, então um teste não suja o outro.
// O banco é um SQLite em memória: o arquivo fiado.db não é tocado.
@DataJpaTest(properties = "spring.datasource.url=jdbc:sqlite::memory:")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsuarioRepositorioTestes {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    private final BCryptPasswordEncoder codificador = new BCryptPasswordEncoder();

    @Test
    void salvaEBuscaUsuarioPeloEmail() {
        String hash = codificador.encode("senha123");
        usuarioRepositorio.save(new Usuario("Maria", "maria@email.com", hash));

        Optional<Usuario> encontrado = usuarioRepositorio.buscarPorEmail("maria@email.com");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getId()).isNotNull();
        assertThat(encontrado.get().getNome()).isEqualTo("Maria");
        // A senha salva é o hash, não o texto digitado, mas ainda confere
        assertThat(encontrado.get().getSenha()).isNotEqualTo("senha123");
        assertThat(codificador.matches("senha123", encontrado.get().getSenha())).isTrue();
    }

    @Test
    void buscarPorEmailInexistenteRetornaVazio() {
        assertThat(usuarioRepositorio.buscarPorEmail("ninguem@email.com")).isEmpty();
    }

    @Test
    void verificaSeEmailJaExiste() {
        usuarioRepositorio.save(new Usuario("João", "joao@email.com", codificador.encode("abc123")));

        assertThat(usuarioRepositorio.existePorEmail("joao@email.com")).isTrue();
        assertThat(usuarioRepositorio.existePorEmail("outro@email.com")).isFalse();
    }
}
