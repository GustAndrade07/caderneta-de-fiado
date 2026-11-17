package br.com.senac.cadernetafiado.seguranca;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.DispatcherType;

/**
 * Quem pode entrar e como.
 *
 * Regra principal: <b>nega por padrão</b>. Só {@code /auth/**} fica aberto;
 * qualquer rota nova que alguém criar daqui para a frente já nasce protegida,
 * sem precisar mexer neste arquivo.
 */
@Configuration
@EnableWebSecurity
public class ConfiguracaoSeguranca {

    @Bean
    SecurityFilterChain correnteDeFiltros(HttpSecurity http, JwtFiltro jwtFiltro,
            TratadorDeErroDeAutenticacao tratadorDeErro) throws Exception {

        http
                // O token vai no cabeçalho, não em cookie: sem sessão e sem CSRF
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas
                        // Um 404 dentro de rota protegida é encaminhado para /error. Esse segundo
                        // despacho não carrega mais a autenticação, então sem esta linha o app
                        // receberia 401 no lugar do 404 e deslogaria o usuário por uma URL errada.
                        // Liberar só o despacho (e não a rota /error) mantém o /error chamado de
                        // fora protegido.
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.ASYNC).permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(tratadorDeErro)
                        .accessDeniedHandler(tratadorDeErro))
                .addFilterBefore(jwtFiltro, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * O filtro não é um {@code @Component} de propósito: um bean do tipo Filter
     * seria registrado também fora da corrente de segurança, rodando duas vezes.
     */
    @Bean
    JwtFiltro jwtFiltro(JwtService jwtService, UsuarioDetalhesService usuarioDetalhesService) {
        return new JwtFiltro(jwtService, usuarioDetalhesService);
    }

    /** Usado pelo cadastro e pelo login: {@code encode(senha)} e {@code matches(digitada, hash)}. */
    @Bean
    PasswordEncoder codificadorDeSenha() {
        return new BCryptPasswordEncoder();
    }

    /** Para quem preferir autenticar pelo Spring em vez de comparar o hash na mão. */
    @Bean
    AuthenticationManager gerenciadorDeAutenticacao(UsuarioDetalhesService usuarioDetalhesService,
            PasswordEncoder codificadorDeSenha) {

        DaoAuthenticationProvider provedor = new DaoAuthenticationProvider(usuarioDetalhesService);
        provedor.setPasswordEncoder(codificadorDeSenha);

        return new ProviderManager(provedor);
    }

    /**
     * O app roda no celular e chama o IP do PC, não o localhost. Por isso as
     * origens vêm de propriedade, com {@code *} no desenvolvimento. Como não
     * usamos cookie, liberar a origem não abre brecha de CSRF.
     */
    @Bean
    CorsConfigurationSource origensLiberadas(@Value("${app.jwt.origens-liberadas:*}") String origens) {
        List<String> lista = Arrays.stream(origens.split(","))
                .map(String::trim)
                .filter(origem -> !origem.isEmpty())
                .toList();

        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOriginPatterns(lista);
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("*"));
        configuracao.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/**", configuracao);

        return fonte;
    }
}
