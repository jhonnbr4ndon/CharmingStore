package com.charmosaPlus.Charmosa.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private final int EXPIRATION_MS = 86400000; // 24 horas

    /**
     * Gera um token JWT com o username e role.
     *
     * @param username O nome de usuário.
     * @param role     A role do usuário (e.g., ROLE_USER, ROLE_ADMIN).
     * @return O token JWT gerado.
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role) // Adiciona a role como claim no token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extrai o username do token.
     *
     * @param token O token JWT.
     * @return O username extraído.
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extrai a role do token.
     *
     * @param token O token JWT.
     * @return A role extraída.
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Valida o token JWT.
     *
     * @param token O token JWT.
     * @return True se o token for válido; false caso contrário.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrai todos os claims do token.
     *
     * @param token O token JWT.
     * @return Os claims do token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
