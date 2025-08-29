package com.intervale.diagnostictool.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "profile_diagnostic_methods")
public class ProfileDiagnosticMethod {
    
    @EmbeddedId
    private ProfileDiagnosticMethodId id = new ProfileDiagnosticMethodId();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("profileId")
    @JoinColumn(name = "profile_id", insertable = false, updatable = false)
    private Profile profile;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("diagnosticMethodId")
    @JoinColumn(name = "diagnostic_method_id", insertable = false, updatable = false)
    private DiagnosticMethod diagnosticMethod;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("deviceId")
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;
    
    @Column(name = "is_active")
    private boolean active = true;
    
    public ProfileDiagnosticMethod(Profile profile, Device device, DiagnosticMethod method) {
        this.profile = profile;
        this.device = device;
        this.diagnosticMethod = method;
        this.id = new ProfileDiagnosticMethodId(profile.getId(), device.getId(), method.getId());
        
        // Add this association to the profile
        if (profile != null) {
            profile.getDeviceDiagnosticMethods().add(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfileDiagnosticMethod that = (ProfileDiagnosticMethod) o;
        return Objects.equals(profile, that.profile) &&
               Objects.equals(diagnosticMethod, that.diagnosticMethod);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(profile, diagnosticMethod);
    }
    
    // Additional getters and setters for Lombok compatibility
    
    public ProfileDiagnosticMethodId getId() {
        return id;
    }
    
    public void setId(ProfileDiagnosticMethodId id) {
        this.id = id;
    }
    
    public Profile getProfile() {
        return profile;
    }
    
    public void setProfile(Profile profile) {
        this.profile = profile;
    }
    
    public DiagnosticMethod getDiagnosticMethod() {
        return diagnosticMethod;
    }
    
    public void setDiagnosticMethod(DiagnosticMethod diagnosticMethod) {
        this.diagnosticMethod = diagnosticMethod;
    }
    
    public Device getDevice() {
        return device;
    }
    
    public void setDevice(Device device) {
        this.device = device;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
}
