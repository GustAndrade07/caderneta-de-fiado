package br.com.senac.cadernetafiado.seguranca;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.senac.cadernetafiado.modelo.Usuario;
import br.com.senac.cadernetafiado.repositorio.UsuarioRepositorio;
import br.com.senac.rotasdeteste.ControladorDeTeste;

/**
 * Os mesmos critérios, mas num servidor de verdade.
 *
 * Existe porque o MockMvc não reproduz o encaminhamento para /error: num
 * servidor real, um 404 dentro de rota protegida vira um segundo despacho
 * (ERROR) e, sem tratar isso, a resposta chega ao app como 401. Foi exatamente
 * o que a conferência no navegador pegou, então virou teste.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:sqlite::memory:")
@Import(ControladorDeTeste.class)
class SegurancaHttpTestes {

    private static final String EMAIL = "teste.http@caderneta.com";

    @LocalServerPort
    private int porta;

    @Autowired
    private JwtService jwt;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private PasswordEncoder codificadorDeSenha;

    private final HttpClient cliente = HttpClient.newHttpClient();

    @BeforeEach
    void criarUsuarioDoTeste() {
        if (!usuarioRepositorio.existePorEmail(EMAIL)) {
            usuarioRepositorio.save(new Usuario("Teste HTTP", EMAIL, codificadorDeSenha.encode("senha123")));
        }
    }

    private HttpResponse<String> chamar(String caminho, String token) throws IOException, InterruptedException {
        HttpRequest.Builder requisicao = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + porta + caminho))
                .GET();

        if (token != null) {
            requisicao.header("Authorization", "Bearer " + token);
        }

        return cliente.send(requisicao.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("Sem token, rota protegida devolve 401 em JSON")
    void semTokenRetorna401() throws Exception {
        HttpResponse<String> resposta = chamar("/teste/protegido", null);

        assertThat(resposta.statusCode()).isEqualTo(401);
        assertThat(resposta.body()).contains("nao_autorizado");
    }

    @Test
    @DisplayName("Com token válido, a rota protegida responde 200")
    void comTokenValidoResponde200() throws Exception {
        HttpResponse<String> resposta = chamar("/teste/protegido", jwt.gerarToken(EMAIL));

        assertThat(resposta.statusCode()).isEqualTo(200);
        assertThat(resposta.body()).startsWith(EMAIL + ":");
    }

    @Test
    @DisplayName("Com token válido, URL que não existe devolve 404 e não 401")
    void urlInexistenteComTokenRetorna404() throws Exception {
        HttpResponse<String> resposta = chamar("/fiados/nao-existe-ainda", jwt.gerarToken(EMAIL));

        assertThat(resposta.statusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("Rota liberada que ainda não existe devolve 404, não 401")
    void rotaDeAuthInexistenteRetorna404() throws Exception {
        HttpResponse<String> resposta = chamar("/auth/login", null);

        assertThat(resposta.statusCode()).isEqualTo(404);
    }
}
