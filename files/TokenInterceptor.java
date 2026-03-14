package org.example.bc_maps_system.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.bc_maps_system.model.Token;
import org.example.bc_maps_system.service.TokenService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    public static final String TOKEN_ATTRIBUTE = "authenticatedToken";

    private final TokenService tokenService;

    public TokenInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String tokenValue = authHeader.substring(7);
        Optional<Token> tokenOpt = tokenService.findByValue(tokenValue);

        if (tokenOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        Token token = tokenOpt.get();

        if (!tokenService.isValid(token)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        request.setAttribute(TOKEN_ATTRIBUTE, token);
        return true;
    }
}