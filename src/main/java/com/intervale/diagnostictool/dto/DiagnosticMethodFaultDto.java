package com.intervale.diagnostictool.dto;

import com.intervale.diagnostictool.model.enums.CoverageLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticMethodFaultDto {
    private Long id;
    private Long diagnosticMethodId;
    private String diagnosticMethodName;
    private String diagnosticMethodDescription;
    private Long faultTypeId;
    private String faultTypeCode;
    private String faultTypeName;
    private CoverageLevel effectiveness;
    private LocalDateTime createdAt;
    
    // Additional fields for UI
    private boolean selected;
}
