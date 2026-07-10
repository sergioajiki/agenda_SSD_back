package com.ssd.agenda_SSD_back.config;

import com.ssd.agenda_SSD_back.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração central de segurança da API.
 *
 * A API é stateless: cada requisição se autentica sozinha via o header
 * "Authorization: Bearer <token>", validado pelo JwtAuthenticationFilter
 * antes de chegar nos controllers — não existe HttpSession nem cookie de
 * login.
 *
 * As regras abaixo ainda são no nível "precisa estar logado ou não". Regras
 * mais finas por role (ex.: só ADMIN pode cadastrar usuário em
 * UserController, e trocar o requestingUserId do MeetingController pelo
 * usuário autenticado) entram numa etapa seguinte do roadmap de segurança —
 * ver o artefato de diagnóstico do projeto.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Substitui os "@CrossOrigin" soltos que existiam em cada controller —
    // agora a política de CORS mora num único lugar e respeita a lista de
    // origens configurada em application.properties (cors.allowed-origins).
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API pura, sem formulário/cookie de sessão — não precisa de proteção CSRF.
                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Sem sessão: cada requisição carrega seu próprio token.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Pré-flight de CORS não manda o header Authorization —
                        // precisa ser liberado antes da regra "anyRequest().authenticated()".
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Login precisa ser público, senão ninguém consegue obter o token.
                        .requestMatchers("/api/user/login").permitAll()
                        // Health check e documentação Swagger também ficam abertos.
                        .requestMatchers("/api/health", "/api/health/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Console H2 segue aberto por enquanto — item já sinalizado
                        // no diagnóstico de segurança do projeto pra ser fechado depois.
                        .requestMatchers("/h2-console/**").permitAll()
                        // Todo o resto exige um token válido.
                        .anyRequest().authenticated()
                )

                // Necessário pro console H2 (usa <frame>) continuar funcionando
                // no navegador mesmo com o cabeçalho de segurança padrão ativo.
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                // O filtro JWT roda antes do filtro padrão de login por formulário.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** Política de CORS única da API — origens vêm de cors.allowed-origins. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
