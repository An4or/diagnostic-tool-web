package com.intervale.diagnostictool.dto.request;

import com.intervale.diagnostictool.model.enums.CoverageLevel;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticMethodFaultRequest {
    
    @NotNull(message = "Diagnostic method ID is required")
    private Long diagnosticMethodId;
    
    @NotNull(message = "Fault type ID is required")
    private Long faultTypeId;
    
    @NotNull(message = "Effectiveness level is required")
    private CoverageLevel effectiveness;
}
