package com.ltp.sudomaster.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveRequest {

    @NotNull(message = "Position must not be null")
    @Min(value = 0, message = "Position must be >= 0")
    @Max(value = 80, message = "Position must be <= 80")
    private Integer position;

    @NotNull(message = "Value must not be null")
    @Min(value = 0, message = "Value must be >= 0")
    @Max(value = 9, message = "Value must be <= 9")
    private Integer value;

    @Builder.Default
    private java.util.List<Integer> candidates = new java.util.ArrayList<>();
}
