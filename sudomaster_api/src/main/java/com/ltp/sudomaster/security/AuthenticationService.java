package com.ltp.sudomaster.security;

import org.springframework.stereotype.Service;
import java.util.Base64;

@Service
public class AuthenticationService {
    
    public String extractCredentials(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return null;
        }
        try {
            String encodedCredentials = authHeader.substring(6);
            byte[] decodedBytes = Base64.getDecoder().decode(encodedCredentials);
            return new String(decodedBytes);
        } catch (Exception e) {
            return null;
        }
    }
    
    public String[] parseCredentials(String credentials) {
        if (credentials == null || !credentials.contains(":")) {
            return null;
        }
        return credentials.split(":", 2);
    }
    
    public String encodeCredentials(String username, String password) {
        String credentials = username + ":" + password;
        return Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}
