package com.intervale.diagnostictool.service;

import com.intervale.diagnostictool.dto.request.DeviceConfigRequest;
import com.intervale.diagnostictool.model.Device;
import com.intervale.diagnostictool.model.Profile;
import com.intervale.diagnostictool.model.ProfileDeviceConfig;
import com.intervale.diagnostictool.model.ProfileDeviceConfigId;
import com.intervale.diagnostictool.repository.DeviceRepository;
import com.intervale.diagnostictool.repository.ProfileDeviceConfigRepository;
import com.intervale.diagnostictool.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileDeviceConfigService {
    
    private final ProfileDeviceConfigRepository configRepository;
    private final ProfileRepository profileRepository;
    private final DeviceRepository deviceRepository;

    public List<ProfileDeviceConfig> findByProfileId(Long profileId) {
        return configRepository.findByProfileId(profileId);
    }

    public Map<Long, ProfileDeviceConfig> getConfigMapForProfile(Long profileId) {
        Map<Long, ProfileDeviceConfig> configMap = new HashMap<>();
        for (ProfileDeviceConfig config : configRepository.findByProfileId(profileId)) {
            configMap.put(config.getDevice().getId(), config);
        }
        return configMap;
    }

    @Transactional
    public ProfileDeviceConfig updateConfig(Long profileId, DeviceConfigRequest request) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found with id: " + profileId));
        
        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new IllegalArgumentException("Device not found with id: " + request.getDeviceId()));

        ProfileDeviceConfigId id = new ProfileDeviceConfigId(profileId, request.getDeviceId());
        Optional<ProfileDeviceConfig> existingConfig = configRepository.findById(id);
        
        ProfileDeviceConfig config;
        if (existingConfig.isPresent()) {
            config = existingConfig.get();
            config.setSValue(request.getSValue());
            config.setLambdaValue(request.getLambdaValue());
        } else {
            config = new ProfileDeviceConfig(profile, device);
            config.setSValue(request.getSValue());
            config.setLambdaValue(request.getLambdaValue());
        }
        
        return configRepository.save(config);
    }

    @Transactional
    public ProfileDeviceConfig updateSValue(Long profileId, Long deviceId, BigDecimal sValue) {
        ProfileDeviceConfigId id = new ProfileDeviceConfigId(profileId, deviceId);
        Optional<ProfileDeviceConfig> existingConfig = configRepository.findById(id);
        
        if (existingConfig.isPresent()) {
            ProfileDeviceConfig config = existingConfig.get();
            config.setSValue(sValue);
            return configRepository.save(config);
        } else {
            Profile profile = profileRepository.findById(profileId)
                    .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
            Device device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new IllegalArgumentException("Device not found"));
            
            ProfileDeviceConfig config = new ProfileDeviceConfig(profile, device);
            config.setSValue(sValue);
            return configRepository.save(config);
        }
    }

    @Transactional
    public ProfileDeviceConfig updateLambdaValue(Long profileId, Long deviceId, BigDecimal lambdaValue) {
        ProfileDeviceConfigId id = new ProfileDeviceConfigId(profileId, deviceId);
        Optional<ProfileDeviceConfig> existingConfig = configRepository.findById(id);
        
        if (existingConfig.isPresent()) {
            ProfileDeviceConfig config = existingConfig.get();
            config.setLambdaValue(lambdaValue);
            return configRepository.save(config);
        } else {
            Profile profile = profileRepository.findById(profileId)
                    .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
            Device device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new IllegalArgumentException("Device not found"));
            
            ProfileDeviceConfig config = new ProfileDeviceConfig(profile, device);
            config.setLambdaValue(lambdaValue);
            return configRepository.save(config);
        }
    }

    public Optional<ProfileDeviceConfig> findByProfileIdAndDeviceId(Long profileId, Long deviceId) {
        return configRepository.findByProfileIdAndDeviceId(profileId, deviceId);
    }
}
