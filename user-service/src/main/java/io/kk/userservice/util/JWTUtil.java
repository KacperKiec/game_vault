package io.kk.userservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.kk.userservice.model.Role;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

@Component
public class JWTUtil {
    @Value("${app.secret-key}")
    private String SECRET_STRING;
    private SecretKey SECRET_KEY;

    @PostConstruct
    public void init() {
        if (SECRET_STRING == null || SECRET_STRING.length() < 32) {
            throw new IllegalArgumentException("Secret key must be at least 32 characters long!");
        }
        this.SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
    }

    public SecretKey getSecretKey() {
        return SECRET_KEY;
    }

    public String generateToken(Long userId, String username, Role role) {
        long expirationTime = 1000 * 60 * 60;

        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role.name())
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SECRET_KEY)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsername(String token) {
        return getClaims(token).get("username", String.class);
    }

    public Long getUserId(String token) {
        return Long.parseLong(Objects.requireNonNull(getClaims(token).getSubject()));
    }

    public Role getRole(String token) {
        String roleString = getClaims(token).get("role", String.class);
        return Role.valueOf(roleString);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
