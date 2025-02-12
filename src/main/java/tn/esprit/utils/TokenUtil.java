package tn.esprit.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import tn.esprit.Models.Role;

import java.util.Date;
import javax.crypto.SecretKey;

public class TokenUtil {
    private static final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION_TIME = 86400000; // 24 hours

    // Generate JWT token
    public static String generateToken(String email, Role role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();
    }

    // Validate JWT token
    public static Jws<Claims> validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
    }

    // Extract email from token
    public static String getEmailFromToken(String token) {
        return validateToken(token).getBody().getSubject();
    }

    // Extract role from token
    public static Role getRoleFromToken(String token) {
        return Role.valueOf(validateToken(token).getBody().get("role", String.class));
    }
}
