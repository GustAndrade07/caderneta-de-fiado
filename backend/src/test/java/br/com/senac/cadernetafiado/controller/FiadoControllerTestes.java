package br.com.senac.cadernetafiado.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import br.com.senac.cadernetafiado.model.Usuario;
import br.com.senac.cadernetafiado.repository.FiadoRepository;
import br.com.senac.cadernetafiado.repository.UsuarioRepository;
import br.com.senac.cadernetafiado.security.JwtService;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite::memory:")
@AutoConfigureMockMvc
class FiadoControllerTestes {
    private static final String EMAIL_A = "a.fiado@caderneta.com";
    private static final String EMAIL_B = "b.fiado@caderneta.com";
    private static final String CORPO = """
            {"nomeCliente":"João","valor":45.90,"data":"2026-11-15","descricao":"Arroz e feijão"}""";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JwtService jwt;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private FiadoRepository fiadoRepository;
    @Autowired
    private PasswordEncoder codificador;

    @BeforeEach
    void preparar() {
        fiadoRepository.deleteAll();
        for (String email : new String[] { EMAIL_A, EMAIL_B }) {
            if (!usuarioRepository.existePorEmail(email)) {
                usuarioRepository.save(new Usuario("Teste", email, codificador.encode("senha123")));
            }
        }
    }

    private MockHttpServletRequestBuilder como(MockHttpServletRequestBuilder req, String email) {
        return req.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.gerarToken(email));
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, String corpo) {
        return req.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    private long criar(String email, String corpo) throws Exception {
        MvcResult r = mvc.perform(como(json(post("/fiados"), corpo), email)).andExpect(status().isCreated()).andReturn();
        return ((Number) com.jayway.jsonpath.JsonPath.read(r.getResponse().getContentAsString(), "$.id")).longValue();
    }

    @Test
    @DisplayName("POST cria fiado não pago, ignorando pago/usuario enviados")
    void criaNaoPago() throws Exception {
        String corpo = """
                {"nomeCliente":"João","valor":45.90,"data":"2026-11-15","pago":true,"usuario":999}""";
        mvc.perform(como(json(post("/fiados"), corpo), EMAIL_A))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pago").value(false))
                .andExpect(jsonPath("$.valor").value(45.90))
                .andExpect(jsonPath("$.nomeCliente").value("João"));
    }

    @Test
    @DisplayName("POST valida campos obrigatórios e valor positivo")
    void validaEntrada() throws Exception {
        mvc.perform(como(json(post("/fiados"), "{\"valor\":10,\"data\":\"2026-11-15\"}"), EMAIL_A))
                .andExpect(status().isBadRequest());
        mvc.perform(como(json(post("/fiados"), "{\"nomeCliente\":\"X\",\"valor\":0,\"data\":\"2026-11-15\"}"), EMAIL_A))
                .andExpect(status().isBadRequest());
        mvc.perform(como(json(post("/fiados"), "{\"nomeCliente\":\"X\",\"valor\":5}"), EMAIL_A))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Sem token devolve 401")
    void semToken() throws Exception {
        mvc.perform(get("/fiados")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET lista só os fiados do usuário")
    void listaSoDoUsuario() throws Exception {
        criar(EMAIL_A, CORPO);
        criar(EMAIL_B, CORPO);
        mvc.perform(como(get("/fiados"), EMAIL_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Usuário B não vê, edita, exclui nem paga fiado do usuário A")
    void isolamento() throws Exception {
        long id = criar(EMAIL_A, CORPO);
        mvc.perform(como(get("/fiados/" + id), EMAIL_B)).andExpect(status().isNotFound());
        mvc.perform(como(json(put("/fiados/" + id), CORPO), EMAIL_B)).andExpect(status().isNotFound());
        mvc.perform(como(delete("/fiados/" + id), EMAIL_B)).andExpect(status().isNotFound());
        mvc.perform(como(patch("/fiados/" + id + "/pagar"), EMAIL_B)).andExpect(status().isNotFound());

        mvc.perform(como(get("/fiados/" + id), EMAIL_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pago").value(false));
    }

    @Test
    @DisplayName("PUT atualiza campos editáveis e mantém pago")
    void atualiza() throws Exception {
        long id = criar(EMAIL_A, CORPO);
        mvc.perform(como(patch("/fiados/" + id + "/pagar"), EMAIL_A)).andExpect(status().isOk());
        String novo = """
                {"nomeCliente":"João Silva","valor":50.00,"data":"2026-11-15","descricao":"Arroz, feijão e óleo","pago":false}""";
        mvc.perform(como(json(put("/fiados/" + id), novo), EMAIL_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCliente").value("João Silva"))
                .andExpect(jsonPath("$.valor").value(50.00))
                .andExpect(jsonPath("$.pago").value(true));
    }

    @Test
    @DisplayName("PATCH pagar marca como pago sem alterar o valor")
    void paga() throws Exception {
        long id = criar(EMAIL_A, CORPO);
        mvc.perform(como(patch("/fiados/" + id + "/pagar"), EMAIL_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pago").value(true))
                .andExpect(jsonPath("$.valor").value(45.90));
    }

    @Test
    @DisplayName("DELETE devolve 204 e depois 404")
    void exclui() throws Exception {
        long id = criar(EMAIL_A, CORPO);
        mvc.perform(como(delete("/fiados/" + id), EMAIL_A)).andExpect(status().isNoContent());
        mvc.perform(como(get("/fiados/" + id), EMAIL_A)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET total soma só os não pagos do usuário")
    void total() throws Exception {
        criar(EMAIL_A, "{\"nomeCliente\":\"A\",\"valor\":30.00,\"data\":\"2026-11-15\"}");
        long pago = criar(EMAIL_A, "{\"nomeCliente\":\"B\",\"valor\":20.00,\"data\":\"2026-11-15\"}");
        criar(EMAIL_A, "{\"nomeCliente\":\"C\",\"valor\":50.00,\"data\":\"2026-11-15\"}");
        criar(EMAIL_B, "{\"nomeCliente\":\"D\",\"valor\":99.00,\"data\":\"2026-11-15\"}");
        mvc.perform(como(patch("/fiados/" + pago + "/pagar"), EMAIL_A)).andExpect(status().isOk());
        mvc.perform(como(get("/fiados/total"), EMAIL_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDevido").value(80.00));
    }
}
