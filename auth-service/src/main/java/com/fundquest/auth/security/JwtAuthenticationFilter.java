package com.fundquest.auth.security;

import com.fundquest.auth.constants.AppConstants;
import com.fundquest.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.fundquest.auth.constants.AppConstants.TOKEN_TYPE_ACCESS;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(AppConstants.AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(AppConstants.JWT_HEADER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String token = authHeader.substring(7);

            if (jwtService.validateToken(token)) {
                String email = jwtService.extractEmailFromToken(token);
                String tokenType = jwtService.extractTokenType(token);

                // Only allow access tokens for authentication
                if (TOKEN_TYPE_ACCESS.equals(tokenType) &&
                        SecurityContextHolder.getContext().getAuthentication() == null) {

                    String role = jwtService.extractRoleFromToken(token);
                    List<String> permissions = jwtService.extractPermissionsFromToken(token);
                    List<GrantedAuthority> authorities = permissions.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    // Add role as authority with ROLE_ prefix for hasRole() support
                    if (role != null && !role.trim().isEmpty()) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(email, null, authorities);

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("Successfully authenticated user: {}", email);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}