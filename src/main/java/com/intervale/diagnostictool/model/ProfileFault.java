package com.intervale.diagnostictool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@Entity
@Table(name = "profile_faults")
public class ProfileFault {
    @EmbeddedId
    private ProfileFaultId id = new ProfileFaultId();

    @JsonIgnore
    @MapsId("profileId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @JsonIgnore
    @MapsId("faultTypeId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fault_type_id", nullable = false)
    private FaultType faultType;

    @Column(name = "is_covered", nullable = false)
    private boolean isCovered = false;

    @Column(name = "covered_methods_ids", columnDefinition = "TEXT")
    private String coveredMethodsIds; // JSON array of method IDs

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ProfileFault() {
    }

    public ProfileFault(Profile profile, FaultType faultType) {
        this.profile = profile;
        this.faultType = faultType;
        this.id = new ProfileFaultId(profile.getId(), faultType.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfileFault)) return false;
        ProfileFault that = (ProfileFault) o;
        return Objects.equals(profile, that.profile) &&
               Objects.equals(faultType, that.faultType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profile, faultType);
    }
}
