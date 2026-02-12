package com.ltp.sudomaster.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckAnswerResponse {

    private boolean correct;
    private String message;
}
