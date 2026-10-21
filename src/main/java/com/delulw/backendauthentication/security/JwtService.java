package com.delulw.backendauthentication.security;

import com.delulw.backendauthentication.auth.dto.AuthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expirationMs;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.expiration-ms}") long expirationMs) {
        if (expirationMs <= 0) {
            throw new IllegalStateException("JWT_EXPIRATION_MS must be greater than zero");
        }
        this.jwtEncoder = jwtEncoder;
        this.expirationMs = expirationMs;
    }

    public AuthResponse createToken(Authentication authentication) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusMillis(expirationMs);
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("backend-authentication")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(authentication.getName())
                .claim("roles", roles)
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new AuthResponse(token, "Bearer", expirationMs / 1000);
    }
}
