package br.com.senac.cadernetafiado.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtServiceTestes {
    private static final String SEGREDO = "chave-de-teste-com-mais-de-32-bytes-para-o-hs256";
    private static final String OUTRO_SEGREDO = "outra-chave-de-teste-com-mais-de-32-bytes-aqui-ok";
    private static final String EMAIL = "demo@caderneta.com";

    private JwtService servico(String segredo, long horas) {
        return new JwtService(segredo, horas, false);
    }

    @Test
    @DisplayName("CA-01: o token tem 3 partes e guarda o e-mail no sub")
    void gerarTokenComEmailNoSub() {
        String token = servico(SEGREDO, 24).gerarToken(EMAIL);

        assertThat(token.split("\\.")).hasSize(3);
        assertThat(servico(SEGREDO, 24).lerEmail(token)).contains(EMAIL);
    }

    @Test
    @DisplayName("CA-02: lê de volta o mesmo e-mail que gerou")
    void leEmailDeTokenValido() {
        JwtService jwt = servico(SEGREDO, 24);

        Optional<String> email = jwt.lerEmail(jwt.gerarToken(EMAIL));

        assertThat(email).contains(EMAIL);
    }

    @Test
    @DisplayName("CA-03: recusa token expirado")
    void recusaTokenExpirado() {
        JwtService vencido = servico(SEGREDO, -1);
        String token = vencido.gerarToken(EMAIL);

        assertThat(servico(SEGREDO, 24).lerEmail(token)).isEmpty();
        assertThat(servico(SEGREDO, 24).tokenValido(token)).isFalse();
    }

    @Test
    @DisplayName("CA-04: recusa token assinado com outra chave")
    void recusaTokenDeOutraChave() {
        String token = servico(OUTRO_SEGREDO, 24).gerarToken(EMAIL);

        assertThat(servico(SEGREDO, 24).lerEmail(token)).isEmpty();
    }

    @Test
    @DisplayName("CA-05: texto que não é token é recusado sem estourar erro")
    void recusaTextoQueNaoEToken() {
        JwtService jwt = servico(SEGREDO, 24);

        assertThat(jwt.lerEmail("abc")).isEmpty();
        assertThat(jwt.lerEmail("")).isEmpty();
        assertThat(jwt.lerEmail(null)).isEmpty();
        assertThat(jwt.tokenValido("abc")).isFalse();
    }

    @Test
    @DisplayName("CA-13: sem a variável de ambiente, em desenvolvimento sobe com chave aleatória")
    void sobeSemVariavelComAviso() {
        JwtService semSegredo = new JwtService("", 24, false);

        String token = semSegredo.gerarToken(EMAIL);

        assertThat(semSegredo.lerEmail(token)).contains(EMAIL);

        assertThat(new JwtService("", 24, false).lerEmail(token)).isEmpty();
    }

    @Test
    @DisplayName("CA-14: em produção, sem a variável de ambiente a aplicação não sobe")
    void falhaEmProducaoSemVariavel() {
        assertThatThrownBy(() -> new JwtService("", 24, true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    @DisplayName("Chave curta demais para HS256 dá mensagem clara")
    void recusaSegredoCurto() {
        assertThatThrownBy(() -> new JwtService("curta", 24, false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");
    }
}
