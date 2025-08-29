package com.intervale.diagnostictool.model.dto;

import com.intervale.diagnostictool.model.DiagnosticMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for {@link DiagnosticMethod} for transferring diagnostic method data to the frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticMethodDTO {
    private Long id;
    private String name;
    private String description;
    private DiagnosticMethod.Effectiveness effectiveness;
    private BigDecimal diagnosticCoverage;
    private DiagnosticMethod.Suitability suitability;
    private String gostReference;
    
    /**
     * Creates a DTO from a DiagnosticMethod entity.
     */
    public static DiagnosticMethodDTO fromEntity(DiagnosticMethod method) {
        if (method == null) {
            return null;
        }
        return new DiagnosticMethodDTO(
            method.getId(),
            method.getName(),
            method.getDescription(),
            method.getEffectiveness(),
            method.getDiagnosticCoverage(),
            method.getSuitability(),
            method.getGostReference()
        );
    }
}
