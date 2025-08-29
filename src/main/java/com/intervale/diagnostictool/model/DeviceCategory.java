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
@ToString(exclude = {"devices", "diagnosticMethods", "faultTypes"})
@Entity
@Table(name = "device_categories")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DeviceCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "gost_reference")
    private String gostReference;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Device> devices = new HashSet<>();
    
    @OneToMany(mappedBy = "deviceCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<DiagnosticMethod> diagnosticMethods = new HashSet<>();
    
    @OneToMany(mappedBy = "deviceCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<FaultType> faultTypes = new HashSet<>();
    
    public DeviceCategory() {}
    
    public DeviceCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public DeviceCategory(String name, String description, String gostReference) {
        this.name = name;
        this.description = description;
        this.gostReference = gostReference;
    }
    
    // Helper methods for bidirectional relationships
    public void addDevice(Device device) {
        devices.add(device);
        device.setCategory(this);
    }
    
    public void removeDevice(Device device) {
        devices.remove(device);
        device.setCategory(null);
    }
    
    // Helper methods for fault types
    public void addFaultType(FaultType faultType) {
        faultTypes.add(faultType);
        faultType.setDeviceCategory(this);
    }
    
    public void removeFaultType(FaultType faultType) {
        faultTypes.remove(faultType);
        faultType.setDeviceCategory(null);
    }
    
    public void addDiagnosticMethod(DiagnosticMethod method) {
        diagnosticMethods.add(method);
        method.setDeviceCategory(this);
    }
    
    public void removeDiagnosticMethod(DiagnosticMethod method) {
        diagnosticMethods.remove(method);
        method.setDeviceCategory(null);
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
    
    public String getGostReference() {
        return gostReference;
    }
    
    public void setGostReference(String gostReference) {
        this.gostReference = gostReference;
    }
    
    public Set<Device> getDevices() {
        return devices;
    }
    
    public void setDevices(Set<Device> devices) {
        this.devices = devices;
    }
    
    public Set<DiagnosticMethod> getDiagnosticMethods() {
        return diagnosticMethods;
    }
    
    public void setDiagnosticMethods(Set<DiagnosticMethod> diagnosticMethods) {
        this.diagnosticMethods = diagnosticMethods;
    }
}
