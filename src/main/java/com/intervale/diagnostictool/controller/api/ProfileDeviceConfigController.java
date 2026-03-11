package com.intervale.diagnostictool.controller.api;

import com.intervale.diagnostictool.dto.request.DeviceConfigRequest;
import com.intervale.diagnostictool.model.ProfileDeviceConfig;
import com.intervale.diagnostictool.service.ProfileDeviceConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profiles/{profileId}/device-config")
@RequiredArgsConstructor
@Tag(name = "Profile Device Config", description = "API for managing device configuration (S and lambda values) in profiles")
public class ProfileDeviceConfigController {
    
    private final ProfileDeviceConfigService configService;

    /**
     * Get all device configurations for a profile.
     */
    @GetMapping
    @Operation(summary = "Get all device configurations for a profile")
    public ResponseEntity<Map<Long, Map<String, Object>>> getAllConfigs(@PathVariable Long profileId) {
        List<ProfileDeviceConfig> configs = configService.findByProfileId(profileId);
        
        Map<Long, Map<String, Object>> result = new HashMap<>();
        for (ProfileDeviceConfig config : configs) {
            Map<String, Object> configData = new HashMap<>();
            configData.put("sValue", config.getSValue());
            configData.put("lambdaValue", config.getLambdaValue());
            result.put(config.getDevice().getId(), configData);
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Update S value for a device.
     */
    @PostMapping("/devices/{deviceId}/s-value")
    @Operation(summary = "Update S value for a device in a profile")
    public ResponseEntity<Map<String, Object>> updateSValue(
            @PathVariable Long profileId,
            @PathVariable Long deviceId,
            @RequestParam BigDecimal value) {
        
        ProfileDeviceConfig config = configService.updateSValue(profileId, deviceId, value);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("sValue", config.getSValue());
        return ResponseEntity.ok(response);
    }

    /**
     * Update lambda value for a device.
     */
    @PostMapping("/devices/{deviceId}/lambda-value")
    @Operation(summary = "Update lambda value for a device in a profile")
    public ResponseEntity<Map<String, Object>> updateLambdaValue(
            @PathVariable Long profileId,
            @PathVariable Long deviceId,
            @RequestParam BigDecimal value) {
        
        ProfileDeviceConfig config = configService.updateLambdaValue(profileId, deviceId, value);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("lambdaValue", config.getLambdaValue());
        return ResponseEntity.ok(response);
    }

    /**
     * Update full device configuration.
     */
    @PostMapping
    @Operation(summary = "Update device configuration (S and lambda values)")
    public ResponseEntity<Map<String, Object>> updateConfig(
            @PathVariable Long profileId,
            @Valid @RequestBody DeviceConfigRequest request) {
        
        ProfileDeviceConfig config = configService.updateConfig(profileId, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("sValue", config.getSValue());
        response.put("lambdaValue", config.getLambdaValue());
        return ResponseEntity.ok(response);
    }
}
