package com.ltp.sudomaster.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String userId;
    private String username;
    private String email;
    
    private String accessToken;
    
    private Long tokenExpiresIn;
    
    private String tokenType;

    @Builder.Default
    private String message = "Authentication successful";

    private String preferencesJson;
}
