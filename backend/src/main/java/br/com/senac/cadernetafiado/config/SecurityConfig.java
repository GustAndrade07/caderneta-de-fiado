package br.com.senac.cadernetafiado.config;

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

import br.com.senac.cadernetafiado.security.JwtFiltro;
import br.com.senac.cadernetafiado.security.JwtService;
import br.com.senac.cadernetafiado.security.TratadorDeErroDeAutenticacao;
import br.com.senac.cadernetafiado.security.UsuarioDetalhesService;
import jakarta.servlet.DispatcherType;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain correnteDeFiltros(HttpSecurity http, JwtFiltro jwtFiltro,
            TratadorDeErroDeAutenticacao tratadorDeErro) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.ASYNC).permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(tratadorDeErro)
                        .accessDeniedHandler(tratadorDeErro))
                .addFilterBefore(jwtFiltro, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    JwtFiltro jwtFiltro(JwtService jwtService, UsuarioDetalhesService usuarioDetalhesService) {
        return new JwtFiltro(jwtService, usuarioDetalhesService);
    }

    @Bean
    PasswordEncoder codificadorDeSenha() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager gerenciadorDeAutenticacao(UsuarioDetalhesService usuarioDetalhesService,
            PasswordEncoder codificadorDeSenha) {
        DaoAuthenticationProvider provedor = new DaoAuthenticationProvider(usuarioDetalhesService);
        provedor.setPasswordEncoder(codificadorDeSenha);

        return new ProviderManager(provedor);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.jwt.origens-liberadas:*}") String origens) {
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
