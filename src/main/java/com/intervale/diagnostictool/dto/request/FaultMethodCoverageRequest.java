package com.intervale.diagnostictool.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaultMethodCoverageRequest {

    @NotNull(message = "Method ID is required")
    private Long methodId;

    @NotNull(message = "Coverage percentage is required")
    @Min(value = 0, message = "Coverage percentage must be at least 0")
    @Max(value = 100, message = "Coverage percentage must be at most 100")
    private Integer coveragePercent;
}

