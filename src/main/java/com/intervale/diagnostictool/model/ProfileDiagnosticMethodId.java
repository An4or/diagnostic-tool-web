package com.intervale.diagnostictool.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ProfileDiagnosticMethodId implements Serializable {
    @Column(name = "profile_id")
    private Long profileId;
    
    @Column(name = "device_id")
    private Long deviceId;
    
    @Column(name = "diagnostic_method_id")
    private Long diagnosticMethodId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfileDiagnosticMethodId that = (ProfileDiagnosticMethodId) o;
        return Objects.equals(profileId, that.profileId) &&
               Objects.equals(deviceId, that.deviceId) &&
               Objects.equals(diagnosticMethodId, that.diagnosticMethodId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileId, deviceId, diagnosticMethodId);
    }
}
