package com.ltp.sudomaster.security;

public interface CredentialValidator {
    
    boolean validateCredentials(String username, String password);
}
