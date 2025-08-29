package com.intervale.diagnostictool.controller;

import com.intervale.diagnostictool.dto.ProfileFaultDto;
import com.intervale.diagnostictool.dto.request.ProfileFaultRequest;
import com.intervale.diagnostictool.model.ProfileFaultId;
import com.intervale.diagnostictool.service.ProfileFaultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profiles/{profileId}/faults")
@RequiredArgsConstructor
@Tag(name = "Profile Fault Management", description = "APIs for managing profile faults")
public class ProfileFaultController {

    private final ProfileFaultService profileFaultService;

    @GetMapping
    @Operation(summary = "Get all faults for a profile")
    public ResponseEntity<List<ProfileFaultDto>> getProfileFaults(@PathVariable Long profileId) {
        return ResponseEntity.ok(profileFaultService.findByProfileId(profileId));
    }

    @GetMapping("/{faultTypeId}")
    @Operation(summary = "Get a specific profile fault by profile ID and fault type ID")
    public ResponseEntity<ProfileFaultDto> getProfileFaultById(
            @PathVariable Long profileId, 
            @PathVariable Long faultTypeId) {
        ProfileFaultId id = new ProfileFaultId(profileId, faultTypeId);
        return ResponseEntity.ok(profileFaultService.findById(id));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get profile faults by device category")
    public ResponseEntity<List<ProfileFaultDto>> getProfileFaultsByCategory(
            @PathVariable Long profileId,
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(profileFaultService.findByProfileIdAndDeviceCategoryId(profileId, categoryId));
    }

    @PostMapping
    @Operation(summary = "Add a fault to a profile")
    public ResponseEntity<ProfileFaultDto> addFaultToProfile(
            @PathVariable Long profileId,
            @Valid @RequestBody ProfileFaultRequest request) {
        // Ensure the profileId in the path matches the one in the request
        if (!profileId.equals(request.getProfileId())) {
            throw new IllegalArgumentException("Profile ID in path must match the one in the request body");
        }
        return new ResponseEntity<>(profileFaultService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{faultTypeId}")
    @Operation(summary = "Update a profile fault")
    public ResponseEntity<ProfileFaultDto> updateProfileFault(
            @PathVariable Long profileId,
            @PathVariable Long faultTypeId,
            @Valid @RequestBody ProfileFaultRequest request) {
        // Ensure the profileId in the path matches the one in the request
        if (!profileId.equals(request.getProfileId())) {
            throw new IllegalArgumentException("Profile ID in path must match the one in the request body");
        }
        ProfileFaultId id = new ProfileFaultId(profileId, faultTypeId);
        return ResponseEntity.ok(profileFaultService.update(id, request));
    }

    @DeleteMapping("/{faultTypeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a fault from a profile")
    public void removeFaultFromProfile(
            @PathVariable Long profileId,
            @PathVariable Long faultTypeId) {
        ProfileFaultId id = new ProfileFaultId(profileId, faultTypeId);
        profileFaultService.delete(id);
    }

    @GetMapping("/coverage")
    @Operation(summary = "Get coverage statistics for a profile")
    public ResponseEntity<Map<String, Object>> getCoverageStats(@PathVariable Long profileId) {
        return ResponseEntity.ok(profileFaultService.getCoverageStats(profileId));
    }
}
