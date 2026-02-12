package com.ltp.sudomaster.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameCompleteRequest {

    @NotNull(message = "Elapsed time is required")
    @Positive(message = "Elapsed time must be positive")
    private Integer elapsedTime;

    @NotNull(message = "Mistakes count is required")
    @Min(value = 0, message = "Mistakes cannot be negative")
    private Integer mistakes;

    @NotNull(message = "Auto-candidate mode flag is required")
    private Boolean autoCandidateMode;
}
