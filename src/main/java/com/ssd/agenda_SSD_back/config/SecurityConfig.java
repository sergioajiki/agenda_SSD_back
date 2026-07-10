package com.ssd.agenda_SSD_back.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssd.agenda_SSD_back.advice.Problem;
import com.ssd.agenda_SSD_back.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
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
 * A listagem de reuniões (GET /api/meeting) é pública de propósito — a
 * agenda é exibida em telas de consulta sem login. Todo o resto exige pelo
 * menos um token válido, e algumas rotas (cadastro de usuário, logs) exigem
 * além disso a role ADMIN via @PreAuthorize direto no controller.
 */
@Configuration
@EnableWebSecurity
// Liga o @PreAuthorize nos controllers (ex.: cadastro de usuário e logs, restritos a ADMIN).
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Substitui os "@CrossOrigin" soltos que existiam em cada controller —
    // agora a política de CORS mora num único lugar e respeita a lista de
    // origens configurada em application.properties (cors.allowed-origins).
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Autowired
    private ObjectMapper objectMapper;

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
                        // A agenda é exibida publicamente em vários lugares (ex.: telas de
                        // consulta) — só criar/editar/excluir reunião exige login, listar não.
                        .requestMatchers(HttpMethod.GET, "/api/meeting", "/api/meeting/**").permitAll()
                        // Health check e documentação Swagger também ficam abertos.
                        .requestMatchers("/api/health", "/api/health/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Página de erro padrão do Spring Boot: quando uma requisição já
                        // autenticada falha por outro motivo (ex.: exceção não tratada),
                        // o servlet container faz um forward interno pra "/error" — sem
                        // liberar essa rota, esse forward esbarra em "anyRequest().authenticated()"
                        // de novo (o filtro JWT não roda 2x na mesma requisição) e o status
                        // real do erro vira 401 de forma enganosa.
                        .requestMatchers("/error").permitAll()
                        // Console H2 segue aberto por enquanto — item já sinalizado
                        // no diagnóstico de segurança do projeto pra ser fechado depois.
                        .requestMatchers("/h2-console/**").permitAll()
                        // Todo o resto (cadastro de usuário, logs, criar/editar/excluir
                        // reunião) exige um token válido. Restrições mais finas por role
                        // (ex.: cadastro/logs só ADMIN) ficam a cargo de @PreAuthorize
                        // direto nos controllers — ver UserController e LogUpdateController.
                        .anyRequest().authenticated()
                )

                // Necessário pro console H2 (usa <frame>) continuar funcionando
                // no navegador mesmo com o cabeçalho de segurança padrão ativo.
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                // O filtro JWT roda antes do filtro padrão de login por formulário.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Corpo de erro consistente com o resto da API (mesmo formato do
                // Problem usado em GeneralControllerAdvice) pras duas situações que
                // o Spring Security intercepta antes de chegar num controller:
                // sem token/token inválido (401) e token válido mas sem permissão
                // de rota (403). Violação de @PreAuthorize dentro de um controller
                // já cai no GeneralControllerAdvice normalmente.
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) ->
                                writeProblem(response, HttpStatus.UNAUTHORIZED,
                                        "Unauthorized", "Autenticação necessária ou token inválido/expirado."))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeProblem(response, HttpStatus.FORBIDDEN,
                                        "Forbidden", "Você não tem permissão para executar esta ação."))
                );

        return http.build();
    }

    private void writeProblem(HttpServletResponse response, HttpStatus status, String message, String detail) throws IOException {
        Problem problem = new Problem(status.value(), message, detail, null);
        response.setStatus(status.value());
        // Sem isso o Writer cai no encoding padrão da JVM/SO (não necessariamente
        // UTF-8), e acento em "detail" vira "?" no corpo da resposta.
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(problem));
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
