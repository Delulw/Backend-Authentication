package com.delulw.backendauthentication.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.delulw.backendauthentication.common.response.ApiError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JsonAuthenticationEntryPoint.class);

    private final ObjectMapper objectMapper;

    public JsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        logger.warn("Unauthenticated request rejected: {} {}", request.getMethod(), request.getRequestURI());
        writeError(
                objectMapper,
                response,
                request,
                HttpStatus.UNAUTHORIZED,
                "Authentication is required");
    }

    static void writeError(
            ObjectMapper objectMapper,
            HttpServletResponse response,
            HttpServletRequest request,
            HttpStatus status,
            String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiError error = new ApiError(
                Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
