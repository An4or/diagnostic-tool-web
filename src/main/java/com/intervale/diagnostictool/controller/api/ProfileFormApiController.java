package com.intervale.diagnostictool.controller.api;

import com.intervale.diagnostictool.model.Device;
import com.intervale.diagnostictool.model.DiagnosticMethod;
import com.intervale.diagnostictool.model.Profile;
import com.intervale.diagnostictool.model.Device.ArchitectureType;
import com.intervale.diagnostictool.model.dto.DiagnosticMethodDTO;
import com.intervale.diagnostictool.service.DeviceService;
import com.intervale.diagnostictool.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for handling AJAX requests from the profile form.
 */
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileFormApiController {

    private final ProfileService profileService;
    private final DeviceService deviceService;

    /**
     * Get all diagnostic methods available for a device in the context of a profile's architecture.
     *
     * @param profileId the ID of the profile
     * @param deviceId the ID of the device
     * @return list of available diagnostic methods
     */
    @GetMapping("/{profileId}/devices/{deviceId}/available-methods")
    public ResponseEntity<List<DiagnosticMethodDTO>> getAvailableMethods(
            @PathVariable Long profileId,
            @PathVariable Long deviceId) {
        
        // Get the device to verify it exists
        Device device = deviceService.findById(deviceId);
        if (device == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Get available methods and convert to DTOs
        List<DiagnosticMethod> methods = profileService.getAvailableDiagnosticMethods(profileId, deviceId);
        List<DiagnosticMethodDTO> dtos = methods.stream()
                .map(DiagnosticMethodDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    
    /**
     * Get the current diagnostic coverage for a profile.
     *
     * @param profileId the ID of the profile
     * @return the diagnostic coverage percentage (0.0 to 100.0)
     */
    @GetMapping("/{profileId}/coverage")
    public ResponseEntity<Double> getDiagnosticCoverage(@PathVariable Long profileId) {
        double coverage = profileService.getDiagnosticCoverage(profileId);
        return ResponseEntity.ok(coverage);
    }
    
    /**
     * Check if the profile meets the SIL requirements.
     *
     * @param profileId the ID of the profile
     * @return true if the profile is compliant, false otherwise
     */
    @GetMapping("/{profileId}/compliance")
    public ResponseEntity<Boolean> checkCompliance(@PathVariable Long profileId) {
        boolean isCompliant = profileService.isProfileCompliant(profileId);
        return ResponseEntity.ok(isCompliant);
    }
    
    /**
     * Update the architecture type for a profile and return the updated coverage.
     *
     * @param profileId the ID of the profile
     * @param architecture the new architecture type
     * @return the updated diagnostic coverage percentage
     */
    @PostMapping("/{profileId}/architecture")
    public ResponseEntity<Double> updateArchitecture(
            @PathVariable Long profileId,
            @RequestParam("architecture") ArchitectureType architecture) {
        
        Profile profile = profileService.findById(profileId);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Update the architecture type
        profile.setArchitectureType(architecture);
        
        // Save the profile to update the coverage
        profileService.updateProfileDiagnosticCoverage(profile);
        
        // Get the updated coverage
        double coverage = profileService.getDiagnosticCoverage(profileId);
        return ResponseEntity.ok(coverage);
    }
}
