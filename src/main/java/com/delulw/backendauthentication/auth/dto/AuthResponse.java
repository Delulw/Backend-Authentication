package com.delulw.backendauthentication.auth.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn) {
}
