package com.ltp.sudomaster.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final long TOKEN_EXPIRY_MS = 72 * 60 * 60 * 1000L;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            
            if (validateAndSetAuthentication(token)) {
                log.debug("Bearer token authenticated");
            } else {
                log.warn("Invalid or expired Bearer token");
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private boolean validateAndSetAuthentication(String token) {
        try {
            String cleanToken = token.replaceAll("\\s", "");
            
            String decodedToken = new String(Base64.getDecoder().decode(cleanToken));
            
            if (!decodedToken.contains(":")) {
                log.warn("Token format invalid - missing separator");
                return false;
            }
            
            String[] parts = decodedToken.split(":", 2);
            if (parts.length != 2) {
                log.warn("Token format invalid - incorrect parts");
                return false;
            }
            
            String userId = parts[0];
            long tokenTimestamp;
            
            try {
                tokenTimestamp = Long.parseLong(parts[1]);
            } catch (NumberFormatException e) {
                log.warn("Token timestamp invalid");
                return false;
            }
            
            long currentTime = System.currentTimeMillis();
            long tokenAge = currentTime - tokenTimestamp;
            
            if (tokenAge < 0) {
                log.warn("Token has future timestamp - rejected");
                return false;
            }
            
            if (tokenAge > TOKEN_EXPIRY_MS) {
                log.warn("Token expired - age: {} ms", tokenAge);
                return false;
            }
            
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userId, token, authorities);
            authentication.setDetails(userId);
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Authenticated userId: {}", userId);
            
            return true;
            
        } catch (IllegalArgumentException e) {
            log.warn("Token decoding failed - invalid Base64");
            return false;
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }
}

