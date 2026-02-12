package com.ltp.sudomaster.security;

import com.ltp.sudomaster.service.UserAuthenticationService;

public class CredentialValidatorImpl implements CredentialValidator {
    
    private final UserAuthenticationService authenticationService;
    
    public CredentialValidatorImpl(UserAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    
    @Override
    public boolean validateCredentials(String username, String password) {
        return authenticationService.validateCredentials(username, password);
    }
}
