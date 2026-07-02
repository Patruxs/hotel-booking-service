package org.example.hotelbookingservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
public class JwtUtils {
    private static final long ACCESS_EXPIRATION_MILLIS = 15L * 60L * 1000L;
    private static final long REFRESH_EXPIRATION_MILLIS = 30L * 24L * 60L * 60L * 1000L;
    private SecretKey key;

    @Value("${jwt.secretKey}")
    private String secretKey;

    @PostConstruct
    private void init() {
        byte[] keyByte = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyByte.length >= 32 ? keyByte : (secretKey + "01234567890123456789012345678901").substring(0, 32).getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email) {
        return generateAccessToken(email, UUID.randomUUID().toString());
    }

    public String generateAccessToken(String email, String jti) {
        return Jwts.builder()
                .subject(email)
                .id(jti)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION_MILLIS))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(UUID accountId, String jti, Instant expiresAt) {
        return Jwts.builder()
                .subject(accountId.toString())
                .id(jti)
                .claim("type", "refresh")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public String getJtiFromToken(String token) {
        return extractClaims(token, Claims::getId);
    }

    public UUID getAccountIdFromRefreshToken(String token) {
        return UUID.fromString(getUsernameFromToken(token));
    }

    public Instant getRefreshExpiresAt() {
        return Instant.now().plusMillis(REFRESH_EXPIRATION_MILLIS);
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
        return claimsTFunction.apply(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload());
    }


    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token, Claims::getExpiration).before(new Date());
    }
}
