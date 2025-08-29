package com.intervale.diagnostictool.dto.request;

import com.intervale.diagnostictool.model.enums.CoverageLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaultTypeRequest {
    
    @NotBlank(message = "Fault type code is required")
    @Size(max = 20, message = "Fault type code must not exceed 20 characters")
    private String code;
    
    @NotBlank(message = "Fault type name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Device category ID is required")
    private Long deviceCategoryId;
    
    @NotNull(message = "Coverage requirement is required")
    private CoverageLevel coverageRequirement;
    
    private String gostReference;
}
