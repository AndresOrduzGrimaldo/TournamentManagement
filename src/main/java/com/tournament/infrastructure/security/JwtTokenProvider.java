package com.tournament.infrastructure.security;

import com.tournament.domain.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Proveedor de tokens JWT para autenticación
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Genera un token JWT para un usuario
     * @param user Usuario para el cual generar el token
     * @return Token JWT
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());
        claims.put("email", user.getEmail());
        
        return createToken(claims, user.getUsername());
    }

    /**
     * Genera un token JWT para autenticación
     * @param authentication Objeto de autenticación
     * @return Token JWT
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return createToken(new HashMap<>(), userDetails.getUsername());
    }

    /**
     * Crea un token JWT con claims específicos
     * @param claims Claims del token
     * @param subject Subject del token
     * @return Token JWT
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extrae el username del token
     * @param token Token JWT
     * @return Username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token
     * @param token Token JWT
     * @return Fecha de expiración
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim específico del token
     * @param token Token JWT
     * @param claimsResolver Función para resolver el claim
     * @return Valor del claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae el ID del usuario del token
     * @param token Token JWT
     * @return ID del usuario
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Extrae el rol del usuario del token
     * @param token Token JWT
     * @return Rol del usuario
     */
    public String extractUserRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * Extrae todos los claims del token
     * @param token Token JWT
     * @return Claims del token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Obtiene la clave de firma
     * @return Clave de firma
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Verifica si el token ha expirado
     * @param token Token JWT
     * @return true si ha expirado
     */
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.warn("Error verificando expiración del token: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Valida un token JWT
     * @param token Token JWT
     * @param userDetails Detalles del usuario
     * @return true si el token es válido
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.warn("Error validando token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Valida un token JWT sin necesidad de UserDetails
     * @param token Token JWT
     * @return true si el token es válido
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el tiempo restante del token en milisegundos
     * @param token Token JWT
     * @return Tiempo restante en milisegundos
     */
    public long getTimeUntilExpiration(String token) {
        try {
            Date expiration = extractExpiration(token);
            Date now = new Date();
            return Math.max(0, expiration.getTime() - now.getTime());
        } catch (Exception e) {
            log.warn("Error obteniendo tiempo de expiración: {}", e.getMessage());
            return 0;
        }
    }
} 