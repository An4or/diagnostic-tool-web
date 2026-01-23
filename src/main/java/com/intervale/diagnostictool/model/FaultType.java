package com.intervale.diagnostictool.model;

import com.intervale.diagnostictool.model.enums.CoverageLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = {"deviceCategory", "device", "diagnosticMethods", "profileFaults"})
@Entity
@Table(name = "fault_types")
public class FaultType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_category_id", nullable = false)
    private DeviceCategory deviceCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(name = "coverage_requirement", nullable = false, length = 20)
    private CoverageLevel coverageRequirement;

    @Column(name = "gost_reference", length = 100)
    private String gostReference;

    @OneToMany(mappedBy = "faultType", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DiagnosticMethodFault> diagnosticMethods = new HashSet<>();

    @OneToMany(mappedBy = "faultType", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProfileFault> profileFaults = new HashSet<>();

    public FaultType() {
    }

    public FaultType(String code, String name, String description, DeviceCategory deviceCategory, Device device,
                    CoverageLevel coverageRequirement, String gostReference) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.deviceCategory = deviceCategory;
        this.device = device;
        this.coverageRequirement = coverageRequirement;
        this.gostReference = gostReference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FaultType)) return false;
        return id != null && id.equals(((FaultType) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
