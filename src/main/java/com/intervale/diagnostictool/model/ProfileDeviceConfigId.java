package com.intervale.diagnostictool.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class ProfileDeviceConfigId implements Serializable {
    
    private Long profileId;
    private Long deviceId;

    public ProfileDeviceConfigId() {
    }

    public ProfileDeviceConfigId(Long profileId, Long deviceId) {
        this.profileId = profileId;
        this.deviceId = deviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfileDeviceConfigId)) return false;
        ProfileDeviceConfigId that = (ProfileDeviceConfigId) o;
        return Objects.equals(profileId, that.profileId) &&
               Objects.equals(deviceId, that.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileId, deviceId);
    }
}
