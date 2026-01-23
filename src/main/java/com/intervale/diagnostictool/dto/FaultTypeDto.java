package com.intervale.diagnostictool.dto;

import com.intervale.diagnostictool.model.enums.CoverageLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaultTypeDto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Long deviceId;
    private String deviceName;
    private Long deviceCategoryId;
    private String deviceCategoryName;
    private CoverageLevel coverageRequirement;
    private String gostReference;
    
    // Additional fields for UI
    private boolean isCovered;
    private String coveredByMethods;
    private String notes;
}
