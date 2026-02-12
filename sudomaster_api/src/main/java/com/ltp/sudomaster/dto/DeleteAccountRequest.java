package com.ltp.sudomaster.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteAccountRequest {
    
    @NotBlank(message = "Password is required to delete account")
    private String password;
}
