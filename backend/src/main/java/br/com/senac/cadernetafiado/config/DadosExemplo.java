package br.com.senac.cadernetafiado.config;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.senac.cadernetafiado.model.Fiado;
import br.com.senac.cadernetafiado.model.Usuario;
import br.com.senac.cadernetafiado.repository.FiadoRepository;
import br.com.senac.cadernetafiado.repository.UsuarioRepository;

@Component
public class DadosExemplo implements CommandLineRunner {
    public static final String EMAIL_DEMO = "demo@caderneta.com";
    public static final String SENHA_DEMO = "senha123";

    private static final Logger registro = LoggerFactory.getLogger(DadosExemplo.class);

    private final UsuarioRepository usuarioRepository;
    private final FiadoRepository fiadoRepository;

    public DadosExemplo(UsuarioRepository usuarioRepository, FiadoRepository fiadoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.fiadoRepository = fiadoRepository;
    }

    @Override
    public void run(String... argumentos) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        String senhaComHash = new BCryptPasswordEncoder().encode(SENHA_DEMO);
        Usuario dono = usuarioRepository.save(new Usuario("Seu Joaquim", EMAIL_DEMO, senhaComHash));

        LocalDate hoje = LocalDate.now();

        fiadoRepository.save(new Fiado("Dona Maria", new BigDecimal("35.90"), hoje.minusDays(1), "Arroz, feijão e óleo", dono));
        fiadoRepository.save(new Fiado("Seu Zé", new BigDecimal("12.50"), hoje.minusDays(3), "Pão e leite", dono));
        fiadoRepository.save(new Fiado("Carla", new BigDecimal("58.00"), hoje.minusDays(7), "Produtos de limpeza", dono));

        Fiado pago1 = new Fiado("Antônio", new BigDecimal("20.00"), hoje.minusDays(10), "Café e açúcar", dono);
        pago1.setPago(true);
        fiadoRepository.save(pago1);

        Fiado pago2 = new Fiado("Dona Maria", new BigDecimal("47.30"), hoje.minusDays(15), "Compras do mês", dono);
        pago2.setPago(true);
        fiadoRepository.save(pago2);

        registro.info("Dados de exemplo criados. Login: {} / {}", EMAIL_DEMO, SENHA_DEMO);
    }
}
