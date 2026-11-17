package br.com.senac.cadernetafiado.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import br.com.senac.cadernetafiado.model.Usuario;
import br.com.senac.cadernetafiado.repository.UsuarioRepository;
import br.com.senac.cadernetafiado.security.JwtService;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite::memory:")
@AutoConfigureMockMvc
class AutenticacaoTestes {
    private static final String EMAIL = "ana@caderneta.com";
    private static final String SENHA = "senha123";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder codificadorDeSenha;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void limparUsuarios() {
        usuarioRepository.deleteAll();
    }

    @Test
    void cadastroComSenhaNoTamanhoMinimoArmazenaSenhaCriptografadaESemExporHash() throws Exception {
        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Ana Silva","email":"ana@caderneta.com","senha":"senha123"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.senha").doesNotExist());

        Usuario usuario = usuarioRepository.buscarPorEmail(EMAIL).orElseThrow();
        assertThat(usuario.getSenha()).isNotEqualTo(SENHA);
        assertThat(codificadorDeSenha.matches(SENHA, usuario.getSenha())).isTrue();
    }

    @Test
    void cadastroComSenhaAcimaDoMinimoEValido() throws Exception {
        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Ana Silva","email":"ana@caderneta.com","senha":"senha1234"}
                        """))
                .andExpect(status().isCreated());
    }

    @Test
    void cadastroDuplicadoRetornaConflitoSemDadosSensíveis() throws Exception {
        cadastrarUsuario();

        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Outra Ana","email":"ana@caderneta.com","senha":"outrasenha"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value("E-mail já cadastrado"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void cadastroComDadosInvalidosRetornaBadRequest() throws Exception {
        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"","email":"invalido","senha":""}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void cadastroComSenhaAbaixoDoMinimoRetornaBadRequest() throws Exception {
        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Ana Silva","email":"ana@caderneta.com","senha":"senha12"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void cadastroComSenhaVaziaRetornaBadRequest() throws Exception {
        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Ana Silva","email":"ana@caderneta.com","senha":""}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void emailMalFormatadoExplicaOFormatoNoCadastroENoLogin() throws Exception {
        String mensagem = "Use o formato nome@dominio.com (ex.: maria@exemplo.com).";

        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Ana Silva","email":"ana-sem-arroba","senha":"senha123"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(mensagem));

        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"ana-sem-arroba","senha":"senha123"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(mensagem));
    }

    @Test
    void cadastroComSenhaCurtaNaoCriaUsuario() throws Exception {
        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Ana Silva","email":"ana@caderneta.com","senha":"curta"}
                        """))
                .andExpect(status().isBadRequest());

        assertThat(usuarioRepository.buscarPorEmail(EMAIL)).isEmpty();
    }

    @Test
    void usuarioRecemCadastradoConsegueEntrarEAcessarSeuPerfil() throws Exception {
        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"  Ana Silva  ","email":"ANA@CADERNETA.COM","senha":"senha123"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.email").value(EMAIL));

        MvcResult login = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"ana@caderneta.com","senha":"senha123"}
                        """))
                .andExpect(status().isOk())
                .andReturn();

        String token = JsonPath.read(login.getResponse().getContentAsString(), "$.token");
        mvc.perform(get("/usuarios/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.nome").value("Ana Silva"));
    
    }

    @Test
    void loginValidoRetornaJwtReconhecidoPeloServico() throws Exception {
        cadastrarUsuario();

        MvcResult resultado = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"ana@caderneta.com","senha":"senha123"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andReturn();

        String token = JsonPath.read(resultado.getResponse().getContentAsString(), "$.token");
        assertThat(jwtService.lerEmail(token)).contains(EMAIL);
    }

    @Test
    void loginInvalidoNaoRevelaSeEmailExiste() throws Exception {
        cadastrarUsuario();

        String corpoSenhaIncorreta = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"ana@caderneta.com","senha":"incorreta"}
                        """))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String corpoEmailInexistente = mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"naoexiste@caderneta.com","senha":"incorreta"}
                        """))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(corpoSenhaIncorreta).isEqualTo(corpoEmailInexistente);
        assertThat(corpoSenhaIncorreta).contains("E-mail ou senha inválidos");
        assertThat(corpoSenhaIncorreta).doesNotContain(EMAIL, SENHA);
    }

    @Test
    void perfilComJwtRetornaSomenteDadosPublicosDoUsuarioAutenticado() throws Exception {
        cadastrarUsuario();
        String token = jwtService.gerarToken(EMAIL);

        mvc.perform(get("/usuarios/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void perfilSemTokenOuComTokenInvalidoRetorna401() throws Exception {
        mvc.perform(get("/usuarios/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mvc.perform(get("/usuarios/me").header(HttpHeaders.AUTHORIZATION, "Bearer token.invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void rotaInexistenteSemTokenRetorna404() throws Exception {
        mvc.perform(get("/auth/rota-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Not Found"))
                .andExpect(jsonPath("$.mensagem").value("Recurso não encontrado"));
    }

    @Test
    void rotaInexistenteComJwtValidoRetorna404PeloTratamentoMvc() throws Exception {
        cadastrarUsuario();
        String token = jwtService.gerarToken(EMAIL);

        mvc.perform(get("/usuarios/rota-inexistente")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Not Found"))
                .andExpect(jsonPath("$.mensagem").value("Recurso não encontrado"));
    }

    @Test
    void perfilSempreCorrespondeAoUsuarioDoTokenSemAceitarIdDeTerceiro() throws Exception {
        cadastrarUsuario();
        Usuario outro = usuarioRepository.save(
                new Usuario("Bruno", "bruno@caderneta.com", codificadorDeSenha.encode("senha456")));
        String tokenDaAna = jwtService.gerarToken(EMAIL);

        mvc.perform(get("/usuarios/me")
                .param("id", outro.getId().toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenDaAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.nome").value("Ana Silva"));
    }

    private void cadastrarUsuario() {
        usuarioRepository.save(new Usuario("Ana Silva", EMAIL, codificadorDeSenha.encode(SENHA)));
    }
}
