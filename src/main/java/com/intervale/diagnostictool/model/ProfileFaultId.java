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
public class ProfileFaultId implements Serializable {
    @Column(name = "profile_id")
    private Long profileId;

    @Column(name = "fault_type_id")
    private Long faultTypeId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfileFaultId)) return false;
        ProfileFaultId that = (ProfileFaultId) o;
        return profileId.equals(that.profileId) &&
               faultTypeId.equals(that.faultTypeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileId, faultTypeId);
    }
}
