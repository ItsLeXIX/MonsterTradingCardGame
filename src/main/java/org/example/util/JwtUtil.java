package org.example.util;

import io.jsonwebtoken.*;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import org.example.repositories.UserRepository;

public class JwtUtil {

    // 256-bit Base64-encoded secret key
    private static final String SECRET_KEY = "U3VwZXJTZWNyZXRLZXlXaXRoU3VwZXJBbGdvcml0aG0=";
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 hours

    // Decode the Base64 key and create a SecretKey object
    private static final SecretKey key = new SecretKeySpec(
            Base64.getDecoder().decode(SECRET_KEY), SignatureAlgorithm.HS256.getJcaName());

    // Generate a JWT token
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) // Store username as subject
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key) // Sign with the fixed key
                .compact();
    }

    // Validate the token and return username
    public static String validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key) // Ensure the same key is used
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject(); // Extract username
            System.out.println("Decoded username: " + username); // Log decoded username
            return username; // Return the username
        } catch (ExpiredJwtException e) {
            System.out.println("Token expired.");
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.out.println("Invalid token.");
            e.printStackTrace();
            return null; // Return null for invalid tokens
        }
    }

    // Validate token and fetch user ID from the database
    public static UUID validateTokenAndGetUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            System.out.println("Extracted username from token: " + username);

            UserRepository userRepository = new UserRepository();
            UUID userId = userRepository.getUserIdByUsername(username);

            System.out.println("Fetched user ID from DB: " + userId);
            return userId;
        } catch (ExpiredJwtException e) {
            System.out.println("Token expired.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}