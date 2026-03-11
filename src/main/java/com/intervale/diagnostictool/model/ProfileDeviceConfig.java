package com.intervale.diagnostictool.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
@ToString
@Entity
@Table(name = "profile_device_config")
public class ProfileDeviceConfig {
    
    @EmbeddedId
    private ProfileDeviceConfigId id = new ProfileDeviceConfigId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("profileId")
    @JoinColumn(name = "profile_id", nullable = false, insertable = false, updatable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("deviceId")
    @JoinColumn(name = "device_id", nullable = false, insertable = false, updatable = false)
    private Device device;

    @Column(name = "s_value", nullable = false, precision = 3, scale = 2)
    private BigDecimal sValue = BigDecimal.valueOf(0.5);

    @Column(name = "lambda_value", nullable = false, precision = 10, scale = 4)
    private BigDecimal lambdaValue = BigDecimal.valueOf(10.0);

    public ProfileDeviceConfig() {
    }

    public ProfileDeviceConfig(Profile profile, Device device) {
        this.profile = profile;
        this.device = device;
        this.id = new ProfileDeviceConfigId(profile.getId(), device.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfileDeviceConfig)) return false;
        ProfileDeviceConfig that = (ProfileDeviceConfig) o;
        return Objects.equals(profile, that.profile) &&
               Objects.equals(device, that.device);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profile, device);
    }
}
