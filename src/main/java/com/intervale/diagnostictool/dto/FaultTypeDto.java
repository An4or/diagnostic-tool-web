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
public class FaultTypeDto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Long deviceCategoryId;
    private String deviceCategoryName;
    private CoverageLevel coverageRequirement;
    private String gostReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for UI
    private boolean isCovered;
    private String coveredByMethods;
    private String notes;
}
