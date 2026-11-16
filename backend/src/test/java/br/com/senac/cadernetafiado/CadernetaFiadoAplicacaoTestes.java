package br.com.senac.cadernetafiado;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Banco em memória para o teste não gravar os dados de exemplo no fiado.db
@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite::memory:")
class CadernetaFiadoAplicacaoTestes {

	@Test
	void aplicacaoInicia() {
	}

}
