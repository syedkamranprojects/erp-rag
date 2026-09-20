package com.erprag.api.security;

import com.erprag.api.config.RagProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final Duration expiry;

    public JwtService(RagProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
        this.expiry = Duration.ofMinutes(properties.getJwt().getExpiryMinutes());
    }

    public String issue(String username, String entityCode, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiry.toMillis());
        return Jwts.builder()
                .subject(username)
                .claim("entityCode", entityCode)
                .claim("role", role)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public EntityUserPrincipal parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new EntityUserPrincipal(claims.getSubject(), claims.get("entityCode", String.class), claims.get("role", String.class));
    }

    public long expirySeconds() {
        return expiry.toSeconds();
    }
}
