package com.ltp.sudomaster.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckAnswerRequest {

    @NotBlank(message = "Session ID is required")
    private String sessionId;

    @NotNull(message = "Row must not be null")
    @Min(value = 0, message = "Row must be >= 0")
    @Max(value = 8, message = "Row must be <= 8")
    private Integer row;

    @NotNull(message = "Column must not be null")
    @Min(value = 0, message = "Column must be >= 0")
    @Max(value = 8, message = "Column must be <= 8")
    @JsonProperty("column")
    private Integer col;

    @NotNull(message = "Value must not be null")
    @Min(value = 1, message = "Value must be between 1 and 9")
    @Max(value = 9, message = "Value must be between 1 and 9")
    private Integer value;
}
