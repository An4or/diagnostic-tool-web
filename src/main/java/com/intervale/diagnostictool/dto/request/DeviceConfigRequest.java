package com.intervale.diagnostictool.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DeviceConfigRequest {
    
    @NotNull
    private Long deviceId;

    @NotNull
    @DecimalMin(value = "0.0", message = "S value must be between 0 and 1")
    @DecimalMax(value = "1.0", message = "S value must be between 0 and 1")
    private BigDecimal sValue;

    @NotNull
    @DecimalMin(value = "0.0", message = "Lambda value must be non-negative")
    private BigDecimal lambdaValue;
}
