package com.ltp.sudomaster.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PauseResumeRequest {

    @NotNull(message = "Action is required")
    private String action;

    @PositiveOrZero(message = "Paused duration must be non-negative")
    private Integer pausedDurationSeconds;
}
