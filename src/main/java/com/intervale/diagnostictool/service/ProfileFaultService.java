package com.intervale.diagnostictool.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intervale.diagnostictool.dto.ProfileFaultDto;
import com.intervale.diagnostictool.dto.request.ProfileFaultRequest;
import com.intervale.diagnostictool.exception.ResourceNotFoundException;
import com.intervale.diagnostictool.mapper.FaultMapper;
import com.intervale.diagnostictool.model.*;
import com.intervale.diagnostictool.model.DiagnosticMethod.CoverageLevel;
import com.intervale.diagnostictool.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileFaultService {

    private final ProfileFaultRepository profileFaultRepository;
    private final ProfileRepository profileRepository;
    private final FaultTypeRepository faultTypeRepository;
    private final DiagnosticMethodRepository diagnosticMethodRepository;
    private final FaultMapper faultMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public void updateFaultStatus(Long profileId, Long deviceId, Long faultId, boolean covered) {
        // Create the composite key
        ProfileFaultId id = new ProfileFaultId(profileId, faultId);
        
        // Try to find the profile fault by composite key
        Optional<ProfileFault> optionalProfileFault = profileFaultRepository.findById(id);
        
        ProfileFault profileFault;
        
        if (optionalProfileFault.isPresent()) {
            // Update existing record
            profileFault = optionalProfileFault.get();
        } else {
            // Create new record if it doesn't exist
            Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", profileId));
                
            FaultType faultType = faultTypeRepository.findById(faultId)
                .orElseThrow(() -> new ResourceNotFoundException("FaultType", "id", faultId));
                
            profileFault = new ProfileFault();
            profileFault.setId(id);
            profileFault.setProfile(profile);
            profileFault.setFaultType(faultType);
        }
        
        // Update the covered status
        profileFault.setCovered(covered);
        profileFaultRepository.save(profileFault);
    }
    
    @Transactional(readOnly = true)
    public List<ProfileFaultDto> findByProfileId(Long profileId) {
        return faultMapper.toProfileFaultDtoList(profileFaultRepository.findByProfileId(profileId));
    }

    @Transactional(readOnly = true)
    public ProfileFaultDto findById(ProfileFaultId id) {
        return faultMapper.toDto(profileFaultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProfileFault", "id", id.toString())));
    }

    @Transactional
    public ProfileFaultDto create(ProfileFaultRequest request) {
        Profile profile = profileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", request.getProfileId()));
        
        FaultType faultType = faultTypeRepository.findById(request.getFaultTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("FaultType", "id", request.getFaultTypeId()));
        
        // Create the composite key
        ProfileFaultId id = new ProfileFaultId(profile.getId(), faultType.getId());
        
        // Check if the relationship already exists
        Optional<ProfileFault> existing = profileFaultRepository.findById(id);
        
        if (existing.isPresent()) {
            return update(id, request);
        }
        
        ProfileFault profileFault = faultMapper.toEntity(request);
        profileFault.setProfile(profile);
        profileFault.setFaultType(faultType);
        
        // Set covered methods if provided
        if (request.getCoveredMethodIds() != null && !request.getCoveredMethodIds().isEmpty()) {
            setCoveredMethods(profileFault, request.getCoveredMethodIds());
        }
        
        return faultMapper.toDto(profileFaultRepository.save(profileFault));
    }

    @Transactional
    public ProfileFaultDto update(ProfileFaultId id, ProfileFaultRequest request) {
        ProfileFault profileFault = profileFaultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProfileFault", "id", id.toString()));
        
        // Update basic fields
        profileFault.setCovered(request.isCovered());
        profileFault.setNotes(request.getNotes());
        
        // Update covered methods if provided
        if (request.getCoveredMethodIds() != null) {
            setCoveredMethods(profileFault, request.getCoveredMethodIds());
        }
        
        // Recalculate coverage based on methods
        updateCoverageStatus(profileFault);
        
        return faultMapper.toDto(profileFaultRepository.save(profileFault));
    }

    @Transactional
    public void delete(ProfileFaultId id) {
        if (!profileFaultRepository.existsById(id)) {
            throw new ResourceNotFoundException("ProfileFault", "id", id.toString());
        }
        profileFaultRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProfileFaultDto> findByProfileIdAndDeviceCategoryId(Long profileId, Long categoryId) {
        return faultMapper.toProfileFaultDtoList(
                profileFaultRepository.findByProfileIdAndDeviceCategoryId(profileId, categoryId));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCoverageStats(Long profileId) {
        long totalFaults = profileFaultRepository.countTotalFaultsByProfileId(profileId);
        long coveredFaults = profileFaultRepository.countCoveredFaultsByProfileId(profileId);
        
        double coveragePercentage = totalFaults > 0 ? (double) coveredFaults / totalFaults * 100 : 0;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFaults", totalFaults);
        stats.put("coveredFaults", coveredFaults);
        stats.put("coveragePercentage", Math.round(coveragePercentage * 100.0) / 100.0);
        
        return stats;
    }

    private void setCoveredMethods(ProfileFault profileFault, List<Long> methodIds) {
        try {
            profileFault.setCoveredMethodsIds(objectMapper.writeValueAsString(methodIds));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing covered method IDs", e);
        }
    }

    private void updateCoverageStatus(ProfileFault profileFault) {
        if (profileFault.getCoveredMethodsIds() == null || profileFault.getCoveredMethodsIds().isEmpty()) {
            profileFault.setCovered(false);
            return;
        }

        try {
            List<Long> methodIds = objectMapper.readValue(
                    profileFault.getCoveredMethodsIds(), 
                    new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {});
            
            if (methodIds.isEmpty()) {
                profileFault.setCovered(false);
                return;
            }
            
            // Get all methods for the fault type
            List<DiagnosticMethod> allMethods = diagnosticMethodRepository
                    .findByFaultTypeId(profileFault.getFaultType().getId());
            
            // Check if all required methods are covered
            Set<Long> requiredMethodIds = allMethods.stream()
                    .filter(method -> method.getCoverageLevel() == CoverageLevel.HIGH)
                    .map(DiagnosticMethod::getId)
                    .collect(Collectors.toSet());
            
            Set<Long> coveredMethodIds = new HashSet<>(methodIds);
            boolean allRequiredCovered = coveredMethodIds.containsAll(requiredMethodIds);
            
            profileFault.setCovered(allRequiredCovered || !requiredMethodIds.isEmpty());
            
        } catch (Exception e) {
            throw new RuntimeException("Error updating coverage status", e);
        }
    }
}
