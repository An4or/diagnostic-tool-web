package com.intervale.diagnostictool.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;
import java.math.BigDecimal;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString(exclude = {"devices", "deviceDiagnosticMethods"})
@Entity
@Table(name = "profiles")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToMany
    @JoinTable(
        name = "profile_devices",
        joinColumns = @JoinColumn(name = "profile_id"),
        inverseJoinColumns = @JoinColumn(name = "device_id")
    )
    private Set<Device> devices = new HashSet<>();
    
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProfileDiagnosticMethod> deviceDiagnosticMethods = new HashSet<>();
    
    @Column(name = "pfd_avg")
    private Double pfdAvg; // Среднее значение PFD
    
    @Column(name = "pfd_max")
    private Double pfdMax; // Максимальное значение PFD
    
    @Column(name = "sil_level")
    private Integer silLevel; // Уровень SIL (1-4)

    @Transient
    public List<Long> getDeviceIds() {
        if (devices == null) {
            return Collections.emptyList();
        }
        return devices.stream()
                    .map(Device::getId)
                    .collect(Collectors.toList());
    }
    
    @Enumerated(EnumType.STRING)
    @Column(name = "architecture_type", nullable = false)
    private Device.ArchitectureType architectureType = Device.ArchitectureType.ONE_OUT_OF_ONE;
    
    @Column(name = "test_interval_days")
    private Integer testIntervalDays; // Интервал тестирования в днях
    
    @Column(name = "total_diagnostic_coverage", precision = 5, scale = 2)
    private BigDecimal totalDiagnosticCoverage = BigDecimal.ZERO;
    
    @Column(name = "is_compliant")
    private Boolean isCompliant = false; // Соответствует ли профиль требованиям SIL
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public Profile() {}
    
    public Profile(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // Helper methods for devices
    public void addDevice(Device device) {
        this.devices.add(device);
        device.getProfiles().add(this);
    }

    public void removeDevice(Device device) {
        this.devices.remove(device);
        device.getProfiles().remove(this);
    }

    // Helper methods for diagnostic methods
    public void addDiagnosticMethod(Device device, DiagnosticMethod method) {
        ProfileDiagnosticMethodId id = new ProfileDiagnosticMethodId(
            this.id, device.getId(), method.getId()
        );
        
        ProfileDiagnosticMethod profileMethod = new ProfileDiagnosticMethod();
        profileMethod.setId(id);
        profileMethod.setProfile(this);
        profileMethod.setDevice(device);
        profileMethod.setDiagnosticMethod(method);
        
        this.deviceDiagnosticMethods.add(profileMethod);
    }

    public void removeDiagnosticMethod(Device device, DiagnosticMethod method) {
        deviceDiagnosticMethods.removeIf(pdm -> 
            pdm.getProfile().equals(this) && 
            pdm.getDevice().equals(device) && 
            pdm.getDiagnosticMethod().equals(method)
        );
    }

    // Updates the diagnostic coverage based on the current set of diagnostic methods
    public void updateDiagnosticCoverage() {
        if (deviceDiagnosticMethods.isEmpty()) {
            this.totalDiagnosticCoverage = BigDecimal.ZERO;
            this.isCompliant = false;
            return;
        }
        
        // Calculate total coverage using the formula: 1 - ∏(1 - DC_i)
        double product = 1.0;
        for (ProfileDiagnosticMethod pdm : deviceDiagnosticMethods) {
            if (pdm.getDiagnosticMethod() != null && 
                pdm.getDiagnosticMethod().getDiagnosticCoverage() != null) {
                
                double coverage = pdm.getDiagnosticMethod().getDiagnosticCoverage().doubleValue() / 100.0;
                product *= (1.0 - coverage);
            }
        }
        
        double totalCoverage = (1.0 - product) * 100.0;
        this.totalDiagnosticCoverage = BigDecimal.valueOf(totalCoverage).setScale(2, BigDecimal.ROUND_HALF_UP);
        
        // Check if coverage meets requirements for the architecture
        updateCompliance();
    }

    // Gets the required coverage percentage based on the architecture type
    private BigDecimal getRequiredCoverageForArchitecture() {
        if (architectureType == null) {
            return BigDecimal.ZERO;
        }
        
        return switch (architectureType) {
            case ONE_OUT_OF_ONE -> new BigDecimal("99.0");
            case ONE_OUT_OF_TWO -> new BigDecimal("90.0");
            case TWO_OUT_OF_THREE -> new BigDecimal("60.0");
        };
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profile profile = (Profile) o;
        return Objects.equals(id, profile.id) &&
               Objects.equals(name, profile.name) &&
               Objects.equals(description, profile.description) &&
               architectureType == profile.architectureType;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, architectureType);
    }
    
    public void removeDiagnosticMethod(ProfileDiagnosticMethod pdm) {
        deviceDiagnosticMethods.remove(pdm);
    }
    
    // Calculate PFD (Probability of Failure on Demand)
    public void calculatePFD() {
        if (devices == null || devices.isEmpty()) {
            this.pfdAvg = 0.0;
            this.pfdMax = 0.0;
            return;
        }
        
        double totalPFD = 0.0;
        double maxPFD = 0.0;
        
        for (Device device : devices) {
            double devicePFD = calculateDevicePFD(device);
            totalPFD += devicePFD;
            maxPFD = Math.max(maxPFD, devicePFD);
        }
        
        this.pfdAvg = totalPFD / devices.size();
        this.pfdMax = maxPFD;
        
        // Determine SIL level based on PFD
        this.silLevel = determineSILLevel(pfdMax);
    }
    
    private double calculateDevicePFD(Device device) {
        // TODO: Implement actual PFD calculation based on device properties
        // and selected diagnostic methods
        return 0.0; // Placeholder
    }
    
    private int determineSILLevel(double pfd) {
        if (pfd <= 1e-5) return 4;
        if (pfd <= 1e-4) return 3;
        if (pfd <= 1e-3) return 2;
        if (pfd <= 1e-2) return 1;
        return 0; // Not compliant
    }
    
    
    /**
     * Update the compliance status based on architecture and coverage.
     */
    private void updateCompliance() {
        if (architectureType == null || totalDiagnosticCoverage == null) {
            this.isCompliant = false;
            return;
        }
        
        BigDecimal requiredCoverage = getRequiredCoverageForArchitecture();
        this.isCompliant = this.totalDiagnosticCoverage.compareTo(requiredCoverage) >= 0;
    }
}
