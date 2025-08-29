package com.intervale.diagnostictool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = {"profiles", "diagnosticMethods"})
@Entity
@Table(name = "devices")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Device {
    
    public enum ArchitectureType {
        ONE_OUT_OF_ONE("1oo1"),
        ONE_OUT_OF_TWO("1oo2"),
        TWO_OUT_OF_THREE("2oo3");

        private final String code;

        ArchitectureType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    private String description;
    
    @Column(name = "channel_count", nullable = false)
    private Integer channelCount = 1;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "architecture_type", nullable = false)
    private ArchitectureType architectureType = ArchitectureType.ONE_OUT_OF_ONE;
    
    @ManyToMany(mappedBy = "devices")
    @JsonIgnore
    private Set<Profile> profiles = new HashSet<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private DeviceCategory category;
    
    @ManyToMany(mappedBy = "devices")
    private Set<DiagnosticMethod> diagnosticMethods = new HashSet<>();
    
    public Device() {}
    
    public Device(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public Device(String name, String description, int channelCount, ArchitectureType architectureType) {
        this.name = name;
        this.description = description;
        this.channelCount = channelCount;
        this.architectureType = architectureType;
    }

    // Additional getters and setters for Lombok compatibility
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getChannelCount() {
        return channelCount;
    }

    public void setChannelCount(Integer channelCount) {
        this.channelCount = channelCount;
    }

    public ArchitectureType getArchitectureType() {
        return architectureType;
    }

    public void setArchitectureType(ArchitectureType architectureType) {
        this.architectureType = architectureType;
    }

    public Set<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(Set<Profile> profiles) {
        this.profiles = profiles;
    }

    public DeviceCategory getCategory() {
        return category;
    }

    public void setCategory(DeviceCategory category) {
        this.category = category;
    }

    public Set<DiagnosticMethod> getDiagnosticMethods() {
        return diagnosticMethods;
    }

    public void setDiagnosticMethods(Set<DiagnosticMethod> diagnosticMethods) {
        this.diagnosticMethods = diagnosticMethods;
    }
}
