package com.intervale.diagnostictool.repository;

import com.intervale.diagnostictool.model.ProfileDeviceConfig;
import com.intervale.diagnostictool.model.ProfileDeviceConfigId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileDeviceConfigRepository extends JpaRepository<ProfileDeviceConfig, ProfileDeviceConfigId> {
    
    List<ProfileDeviceConfig> findByProfileId(Long profileId);
    
    Optional<ProfileDeviceConfig> findByProfileIdAndDeviceId(Long profileId, Long deviceId);
    
    void deleteByProfileId(Long profileId);
}
