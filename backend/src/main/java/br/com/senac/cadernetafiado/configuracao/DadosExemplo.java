package br.com.senac.cadernetafiado.configuracao;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.senac.cadernetafiado.modelo.Fiado;
import br.com.senac.cadernetafiado.modelo.Usuario;
import br.com.senac.cadernetafiado.repositorio.FiadoRepositorio;
import br.com.senac.cadernetafiado.repositorio.UsuarioRepositorio;

/**
 * Coloca dados de exemplo no banco para a demonstração.
 * CommandLineRunner: o Spring executa o método run() logo depois que a aplicação sobe.
 * Só insere se o banco estiver vazio, então rodar o app várias vezes não duplica nada.
 */
@Component
public class DadosExemplo implements CommandLineRunner {

    // Login da demonstração (dados fictícios, só para testar o app)
    public static final String EMAIL_DEMO = "demo@caderneta.com";
    public static final String SENHA_DEMO = "senha123";

    private static final Logger registro = LoggerFactory.getLogger(DadosExemplo.class);

    private final UsuarioRepositorio usuarioRepositorio;
    private final FiadoRepositorio fiadoRepositorio;

    // Injeção pelo construtor: o Spring entrega os repositórios prontos
    public DadosExemplo(UsuarioRepositorio usuarioRepositorio, FiadoRepositorio fiadoRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.fiadoRepositorio = fiadoRepositorio;
    }

    @Override
    public void run(String... argumentos) {
        // Se já existe algum usuário, o banco não está vazio: não faz nada
        if (usuarioRepositorio.count() > 0) {
            return;
        }

        // A senha é salva já com hash BCrypt, igual ao cadastro de verdade
        String senhaComHash = new BCryptPasswordEncoder().encode(SENHA_DEMO);
        Usuario dono = usuarioRepositorio.save(new Usuario("Seu Joaquim", EMAIL_DEMO, senhaComHash));

        // Datas relativas a hoje, para a lista sempre parecer atual na apresentação
        LocalDate hoje = LocalDate.now();

        fiadoRepositorio.save(new Fiado("Dona Maria", new BigDecimal("35.90"), hoje.minusDays(1), "Arroz, feijão e óleo", dono));
        fiadoRepositorio.save(new Fiado("Seu Zé", new BigDecimal("12.50"), hoje.minusDays(3), "Pão e leite", dono));
        fiadoRepositorio.save(new Fiado("Carla", new BigDecimal("58.00"), hoje.minusDays(7), "Produtos de limpeza", dono));

        Fiado pago1 = new Fiado("Antônio", new BigDecimal("20.00"), hoje.minusDays(10), "Café e açúcar", dono);
        pago1.setPago(true);
        fiadoRepositorio.save(pago1);

        Fiado pago2 = new Fiado("Dona Maria", new BigDecimal("47.30"), hoje.minusDays(15), "Compras do mês", dono);
        pago2.setPago(true);
        fiadoRepositorio.save(pago2);

        registro.info("Dados de exemplo criados. Login: {} / {}", EMAIL_DEMO, SENHA_DEMO);
    }
}
