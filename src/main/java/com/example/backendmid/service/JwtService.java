package com.example.backendmid.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey getSigningKey() {

        byte[] keyBytes = Decoders.BASE64.decode(secret);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generaToken(String username) {

        Date adesso = new Date();

        Date scadenza = new Date(adesso.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(adesso)
                .expiration(scadenza)
                .signWith(getSigningKey())
                .compact();
    }

    private Claims estraiClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String estraiUsername(String token) {

        return estraiClaims(token).getSubject();
    }
}
