package com.ssd.agenda_SSD_back.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Roda uma vez por requisição, antes dela chegar no controller: lê o header
 * "Authorization: Bearer <token>", valida o JWT e, se for válido, autentica
 * a requisição no SecurityContext — é isso que faz o restante do Spring
 * Security (authorizeHttpRequests, futuramente @PreAuthorize) saber quem
 * está chamando e com que role.
 *
 * Se não houver header, ou o token for inválido/expirado/de um usuário que
 * não existe mais, a requisição simplesmente segue sem autenticação — quem
 * decide se isso é um problema é o SecurityConfig (endpoint público ou não).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // remove o prefixo "Bearer "

        try {
            String email = jwtService.extractEmail(token);

            // Só autentica se ainda não houver autenticação no contexto
            // (evita reprocessar em cadeias de filtro) e se o token for válido.
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Usuário desativado pelo admin não autentica mais, mesmo com
                // um token emitido antes da desativação e ainda dentro da
                // validade — sem isso, "desativar" não teria efeito imediato.
                if (jwtService.isTokenValid(token, userDetails.getUsername()) && userDetails.isEnabled()) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException | UsernameNotFoundException ex) {
            // Token malformado, expirado, ou usuário do token não existe mais.
            // Segue sem autenticar — o SecurityConfig barra como anônimo se precisar.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
