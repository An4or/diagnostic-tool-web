package com.intervale.diagnostictool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for {@link com.intervale.diagnostictool.model.DiagnosticMethod}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticMethodDto {
    private Long id;
    private String name;
    private String description;
    private String implementationClass;
    private String parameters;
    private String returnType;
    private String category;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for UI
    private Long deviceCategoryId;
    private String deviceCategoryName;
    private Integer coveragePercentage;
}
