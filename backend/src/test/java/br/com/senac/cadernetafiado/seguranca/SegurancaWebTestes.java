package br.com.senac.cadernetafiado.seguranca;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import br.com.senac.cadernetafiado.modelo.Usuario;
import br.com.senac.cadernetafiado.repositorio.UsuarioRepositorio;
import br.com.senac.rotasdeteste.ControladorDeTeste;

/**
 * Critérios CA-06 a CA-12 da Spec de Segurança JWT: o filtro, as rotas
 * liberadas e o usuário logado chegando no controller.
 *
 * As rotas aqui existem só no teste: assim eu provo o filtro sem depender dos
 * endpoints do Pedro e do Ryan, que ainda não foram escritos.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite::memory:")
@AutoConfigureMockMvc
@Import(ControladorDeTeste.class)
class SegurancaWebTestes {

    private static final String EMAIL = "teste.seguranca@caderneta.com";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtService jwt;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private PasswordEncoder codificadorDeSenha;

    @BeforeEach
    void criarUsuarioDoTeste() {
        if (!usuarioRepositorio.existePorEmail(EMAIL)) {
            usuarioRepositorio.save(new Usuario("Teste", EMAIL, codificadorDeSenha.encode("senha123")));
        }
    }

    private String autorizacao(String email) {
        return "Bearer " + jwt.gerarToken(email);
    }

    @Test
    @DisplayName("CA-06: rota protegida sem token devolve 401 em JSON")
    void semTokenRetorna401() throws Exception {
        mvc.perform(get("/teste/protegido"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.erro").value("nao_autorizado"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("CA-07 e CA-11: com token válido a requisição chega no controller com o usuário certo")
    void comTokenValidoChegaNoController() throws Exception {
        mvc.perform(get("/teste/protegido").header(HttpHeaders.AUTHORIZATION, autorizacao(EMAIL)))
                .andExpect(status().isOk())
                .andExpect(content().string(startsWith(EMAIL + ":")));
    }

    @Test
    @DisplayName("CA-08: token adulterado devolve 401, não 500")
    void tokenInvalidoRetorna401() throws Exception {
        mvc.perform(get("/teste/protegido").header(HttpHeaders.AUTHORIZATION, "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/teste/protegido").header(HttpHeaders.AUTHORIZATION, "Bearer "))
                .andExpect(status().isUnauthorized());

        // cabeçalho sem o "Bearer" também não autentica
        mvc.perform(get("/teste/protegido").header(HttpHeaders.AUTHORIZATION, jwt.gerarToken(EMAIL)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CA-09: as rotas de /auth continuam abertas")
    void rotaDeAuthFicaLiberada() throws Exception {
        mvc.perform(get("/auth/teste"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CA-10: rota nova nasce protegida, sem ninguém configurar nada")
    void rotaNovaNasceProtegida() throws Exception {
        mvc.perform(get("/teste/rota-nova"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CA-12: token de usuário que não existe mais devolve 401")
    void tokenDeUsuarioInexistenteRetorna401() throws Exception {
        mvc.perform(get("/teste/protegido").header(HttpHeaders.AUTHORIZATION, autorizacao("fantasma@caderneta.com")))
                .andExpect(status().isUnauthorized());
    }
}
