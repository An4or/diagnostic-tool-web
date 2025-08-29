package com.intervale.diagnostictool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileFaultDto {
    private Long id;
    private Long profileId;
    private Long faultTypeId;
    private String faultTypeCode;
    private String faultTypeName;
    private String faultTypeDescription;
    private String coverageRequirement;
    private Boolean covered;
    private String coveredMethodsIds; // JSON array of method IDs
    private List<Long> coveredMethods = new ArrayList<>(); // For easier processing
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for UI
    private String deviceCategoryName;
    private List<DiagnosticMethodDto> availableMethods = new ArrayList<>();
}
