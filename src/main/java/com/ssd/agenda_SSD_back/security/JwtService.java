package com.ssd.agenda_SSD_back.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Gera e valida o token JWT emitido no login.
 *
 * O token carrega o email do usuário (subject) e a role (claim "role"), pra
 * não precisar consultar o banco só pra saber a permissão em cada requisição
 * — a role fica "congelada" no token até ele expirar ou o usuário logar de
 * novo, então uma troca de role no banco só vale a partir do próximo login.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    /** Monta a chave de assinatura a partir do segredo configurado (HMAC-SHA). */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** Gera um token novo pro usuário que acabou de logar com sucesso. */
    public String generateToken(String email, String role) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    /** Extrai o email (subject) gravado no token. */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /** Extrai a role gravada no token no momento do login. */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Confere se o token é válido pro usuário esperado: assinatura correta,
     * ainda não expirado e o subject bate com o email informado.
     */
    public boolean isTokenValid(String token, String expectedEmail) {
        String email = extractEmail(token);
        return email.equals(expectedEmail) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /** Valida a assinatura e decodifica os claims — lança JwtException se o token for inválido/expirado. */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
