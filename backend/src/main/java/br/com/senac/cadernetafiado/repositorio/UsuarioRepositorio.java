package br.com.senac.cadernetafiado.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.senac.cadernetafiado.modelo.Usuario;

/**
 * Acesso à tabela de usuários. O JpaRepository já traz save, findById,
 * findAll e deleteById; aqui ficam só as consultas próprias do projeto.
 */
public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    // Usado no login: encontra o usuário pelo e-mail (pode não existir)
    @Query("SELECT u FROM Usuario u WHERE u.email = :email")
    Optional<Usuario> buscarPorEmail(@Param("email") String email);

    // Usado no cadastro: impede e-mail repetido
    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.email = :email")
    boolean existePorEmail(@Param("email") String email);
}
