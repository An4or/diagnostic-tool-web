package com.intervale.diagnostictool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import com.intervale.diagnostictool.model.Device.ArchitectureType;

/**
 * Represents a diagnostic method that can be applied to devices.
 * Each method has an effectiveness level (LOW, MEDIUM, HIGH) and a diagnostic coverage percentage.
 */
@Getter
@Setter
@ToString(exclude = "devices")
@Entity
@Table(name = "diagnostic_methods")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DiagnosticMethod {

    /**
     * Coverage level for diagnostic methods
     */
    public enum CoverageLevel {
        LOW,
        MEDIUM,
        HIGH
    }

    /**
     * Implementation complexity levels
     */
    public enum ImplementationComplexity {
        LOW,
        MEDIUM,
        HIGH
    }

    /**
     * Effectiveness levels for diagnostic methods
     */
    public enum Effectiveness {
        LOW,
        MEDIUM,
        HIGH
    }

    /**
     * Suitability levels for diagnostic methods
     */
    public enum Suitability {
        LOW,
        MEDIUM,
        HIGH
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_category_id", nullable = false)
    @JsonIgnore
    private DeviceCategory deviceCategory;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "effectiveness", nullable = false)
    private Effectiveness effectiveness = Effectiveness.MEDIUM;

    @Column(name = "diagnostic_coverage", nullable = false, precision = 5, scale = 2)
    private BigDecimal diagnosticCoverage = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "suitability", nullable = false)
    private Suitability suitability = Suitability.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "implementation_complexity", nullable = false)
    private ImplementationComplexity implementationComplexity = ImplementationComplexity.MEDIUM;

    @Column(name = "gost_reference")
    private String gostReference;

    @Column(name = "coverage_level")
    @Enumerated(EnumType.STRING)
    private CoverageLevel coverageLevel = CoverageLevel.MEDIUM;

    @Column(name = "coverage_percent")
    private BigDecimal coveragePercent = BigDecimal.ZERO;

    @ManyToMany
    @JoinTable(
            name = "device_diagnostic_methods",
            joinColumns = @JoinColumn(name = "diagnostic_method_id"),
            inverseJoinColumns = @JoinColumn(name = "device_id")
    )
    private Set<Device> devices = new HashSet<>();

    public DiagnosticMethod() {
    }

    public DiagnosticMethod(String name, String description,
                            CoverageLevel coverageLevel, BigDecimal coveragePercent,
                            ImplementationComplexity implementationComplexity, String gostReference) {
        this.name = name;
        this.description = description;
        this.coverageLevel = coverageLevel;
        this.coveragePercent = coveragePercent;
        this.implementationComplexity = implementationComplexity;
        this.gostReference = gostReference;
    }

    /**
     * Minimum required diagnostic coverage based on architecture
     */
    @Transient
    public BigDecimal getRequiredCoverage() {
        return switch (effectiveness) {
            case HIGH -> new BigDecimal("99.0");
            case MEDIUM -> new BigDecimal("90.0");
            case LOW -> new BigDecimal("60.0");
        };
    }

    /**
     * Check if this method is suitable for the given architecture
     */
    public boolean isSuitableForArchitecture(ArchitectureType architecture) {
        if (architecture == null) {
            return false;
        }
        // HIGH coverage methods are suitable for all architectures
        if (coverageLevel == CoverageLevel.HIGH) {
            return true;
        }
        return switch (architecture) {
            case ONE_OUT_OF_ONE -> coverageLevel == CoverageLevel.LOW;
            case ONE_OUT_OF_TWO -> coverageLevel == CoverageLevel.LOW || coverageLevel == CoverageLevel.MEDIUM;
            case TWO_OUT_OF_THREE -> true;
        };
    }
    
    /**
     * Comparator for sorting DiagnosticMethod by coverage percentage in ascending order
     */
    public static int compareByCoveragePercent(DiagnosticMethod m1, DiagnosticMethod m2) {
        // Sort in descending order (60%, 90%, 99%)
        return m1.getCoveragePercent().compareTo(m2.getCoveragePercent());
    }
}

