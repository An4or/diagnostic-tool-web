package com.intervale.diagnostictool.service;

import com.intervale.diagnostictool.exception.ResourceNotFoundException;
import com.intervale.diagnostictool.model.*;
import com.intervale.diagnostictool.model.Device.ArchitectureType;
import com.intervale.diagnostictool.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final DeviceRepository deviceRepository;
    private final DiagnosticMethodRepository diagnosticMethodRepository;
    private final ProfileDiagnosticMethodRepository profileDiagnosticMethodRepository;
    private final DeviceService deviceService;

    @Transactional(readOnly = true)
    public List<Profile> findAll() {
        return profileRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Profile> findAllWithDetails() {
        return profileRepository.findAll().stream()
                .map(profile -> profileRepository.findByIdWithAllDetails(profile.getId())
                        .orElse(profile))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Profile findById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with id: " + id));
    }
    
    @Transactional(readOnly = true)
    public Profile findByIdWithDetails(Long id) {
        return profileRepository.findByIdWithAllDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with id: " + id));
    }

    @Transactional
    public Profile create(Profile profile, Set<Long> deviceIds, Map<Long, Set<Long>> deviceMethods) {
        // Set default architecture type if not provided
        if (profile.getArchitectureType() == null) {
            profile.setArchitectureType(ArchitectureType.ONE_OUT_OF_ONE);
        }
        if (profileRepository.existsByName(profile.getName())) {
            throw new IllegalStateException("Profile with name " + profile.getName() + " already exists");
        }
        
        // Set default values
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        
        Profile savedProfile = profileRepository.save(profile);
        
        // Add devices to profile
        if (deviceIds != null && !deviceIds.isEmpty()) {
            // Get all devices with their diagnostic methods in one query
            List<Device> devices = deviceRepository.findByIdsWithDiagnosticMethods(deviceIds);
            
            for (Device device : devices) {
                savedProfile.addDevice(device);
                
                // Add selected diagnostic methods for this device
                if (deviceMethods != null && deviceMethods.containsKey(device.getId())) {
                    Set<Long> methodIds = deviceMethods.get(device.getId());
                    if (methodIds != null && !methodIds.isEmpty()) {
                        for (Long methodId : methodIds) {
                            // Check if the method is valid for this device
                            boolean methodExists = device.getDiagnosticMethods().stream()
                                    .anyMatch(m -> m.getId().equals(methodId));
                                    
                            if (methodExists) {
                                DiagnosticMethod method = diagnosticMethodRepository.findById(methodId)
                                        .orElseThrow(() -> new ResourceNotFoundException("Diagnostic method not found with id: " + methodId));
                                
                                savedProfile.addDiagnosticMethod(device, method);
                            }
                        }
                    }
                }
            }
            
            // Calculate PFD and SIL after all devices and methods are added
            calculateAndUpdateProfileMetrics(savedProfile);
            profileRepository.save(savedProfile);
        }
        
        return savedProfile;
    }

    @Transactional
    public Profile update(Long id, Profile profileDetails, Set<Long> deviceIds, Map<Long, Set<Long>> deviceMethods) {
        Profile profile = findById(id);
        
        if (!profile.getName().equals(profileDetails.getName()) && 
            profileRepository.existsByName(profileDetails.getName())) {
            throw new IllegalStateException("Profile with name " + profileDetails.getName() + " already exists");
        }
        
        // Ensure architecture type is set
        if (profileDetails.getArchitectureType() == null) {
            profileDetails.setArchitectureType(ArchitectureType.ONE_OUT_OF_ONE);
        }
        // Set default architecture type if not provided
        if (profile.getArchitectureType() == null) {
            profile.setArchitectureType(ArchitectureType.ONE_OUT_OF_ONE);
        }
        profile.setName(profileDetails.getName());
        profile.setDescription(profileDetails.getDescription());
        
        // Update devices
        if (deviceIds != null) {
            profile.getDevices().clear();
            Set<Device> devices = new HashSet<>(deviceRepository.findAllByIdIn(deviceIds));
            devices.forEach(profile::addDevice);
        }
        
        // Update diagnostic methods for devices
        if (deviceMethods != null) {
            for (Map.Entry<Long, Set<Long>> entry : deviceMethods.entrySet()) {
                Long deviceId = entry.getKey();
                Set<Long> methodIds = entry.getValue();
                
                Device device = deviceService.findById(deviceId);
                profile.getDeviceDiagnosticMethods().removeIf(pdm -> pdm.getDevice().equals(device));
                
                if (methodIds != null && !methodIds.isEmpty()) {
                    List<DiagnosticMethod> methods = diagnosticMethodRepository.findAllById(methodIds);
                    
                    // Filter methods to only include those that belong to the device's category
                    methods = methods.stream()
                            .filter(method -> method.getDeviceCategory().equals(device.getCategory()))
                            .collect(Collectors.toList());
                    
                    // Create new profile-diagnostic-method associations
                    for (DiagnosticMethod method : methods) {
                        profile.addDiagnosticMethod(device, method);
                    }
                }
            }
        }
        
        // Recalculate metrics
        calculateAndUpdateProfileMetrics(profile);
        return profileRepository.save(profile);
    }

    @Transactional
    public void delete(Long id) {
        Profile profile = findById(id);
        // This will cascade delete all related profile_diagnostic_methods entries
        profileRepository.delete(profile);
    }
    
    @Transactional
    public Profile addDeviceToProfile(Long profileId, Long deviceId, Set<Long> methodIds) {
        Profile profile = findById(profileId);
        Device device = deviceService.findById(deviceId);
        
        // Check if device is already in profile
        if (profile.getDevices().contains(device)) {
            throw new IllegalStateException("Device is already in this profile");
        }
        
        // Add the device to the profile
        profile.addDevice(device);
        
        // Add selected diagnostic methods
        if (methodIds != null && !methodIds.isEmpty()) {
            List<DiagnosticMethod> methods = diagnosticMethodRepository.findAllById(methodIds);
            
            // Filter methods to only include those that belong to the device's category
            methods = methods.stream()
                    .filter(method -> method.getDeviceCategory().equals(device.getCategory()))
                    .collect(Collectors.toList());
            
            // Create profile-diagnostic-method associations
            for (DiagnosticMethod method : methods) {
                profile.addDiagnosticMethod(device, method);
            }
        }
        
        // Recalculate metrics
        calculateAndUpdateProfileMetrics(profile);
        return profileRepository.save(profile);
    }
    
    @Transactional
    public Profile updateDeviceMethods(Long profileId, Long deviceId, Set<Long> methodIds) {
        Profile profile = findById(profileId);
        Device device = deviceService.findById(deviceId);
        
        // Remove all existing method associations for this device in the profile
        Set<ProfileDiagnosticMethod> methodsToRemove = profile.getDeviceDiagnosticMethods().stream()
                .filter(pdm -> pdm.getDevice().equals(device))
                .collect(Collectors.toSet());
                
        for (ProfileDiagnosticMethod pdm : methodsToRemove) {
            profile.removeDiagnosticMethod(pdm);
        }
        
        // Add the new method associations
        if (methodIds != null && !methodIds.isEmpty()) {
            List<DiagnosticMethod> methods = diagnosticMethodRepository.findAllById(methodIds);
            
            // Filter methods to only include those that belong to the device's category
            methods = methods.stream()
                    .filter(method -> method.getDeviceCategory().equals(device.getCategory()))
                    .collect(Collectors.toList());
            
            // Create new profile-diagnostic-method associations
            for (DiagnosticMethod method : methods) {
                profile.addDiagnosticMethod(device, method);
            }
        }
        
        // Recalculate metrics
        calculateAndUpdateProfileMetrics(profile);
        return profileRepository.save(profile);
    }
    
    /**
     * Calculates PFD (Probability of Failure on Demand) and SIL (Safety Integrity Level)
     * for the profile based on its devices and diagnostic methods.
     *
     * @param profile the profile to update metrics for
     */
    @Transactional
    public void calculateAndUpdateProfileMetrics(Profile profile) {
        // Delegate the PFD calculation to the Profile entity
        profile.calculatePFD();
        
        // Calculate total diagnostic coverage
        profile.updateDiagnosticCoverage();
        
        // Save the updated profile
        profileRepository.save(profile);
    }
    
    @Transactional(readOnly = true)
    public List<DiagnosticMethod> getAvailableDiagnosticMethods(Long profileId, Long deviceId) {
        Profile profile = findById(profileId);
        Device device = deviceService.findById(deviceId);
        
        // Get all methods available for this device's category
        List<DiagnosticMethod> availableMethods = 
            diagnosticMethodRepository.findByDeviceCategory(device.getCategory());
            
        // Get methods already selected for this device in this profile
        Set<Long> selectedMethodIds = new HashSet<>();
        for (ProfileDiagnosticMethod pdm : profile.getDeviceDiagnosticMethods()) {
            if (pdm.getDevice() != null && pdm.getDevice().equals(device) && pdm.getDiagnosticMethod() != null) {
                selectedMethodIds.add(pdm.getDiagnosticMethod().getId());
            }
        }
        
        // The controller will convert these to DTOs with the selected status
        return availableMethods;
    }
    
    @Transactional(readOnly = true)
    public double getDiagnosticCoverage(Long profileId) {
        Profile profile = findById(profileId);
        // The coverage is already calculated in the Profile entity
        return profile.getTotalDiagnosticCoverage() != null ? 
               profile.getTotalDiagnosticCoverage().doubleValue() : 0.0;
    }
    
    @Transactional(readOnly = true)
    public boolean isProfileCompliant(Long profileId) {
        Profile profile = findById(profileId);
        // The compliance is already calculated in the Profile entity
        return Boolean.TRUE.equals(profile.getIsCompliant());
    }
    
    @Transactional
    public void updateProfileDiagnosticCoverage(Profile profile) {
        // Calculate and update diagnostic coverage
        profile.updateDiagnosticCoverage();
        // Save the updated profile
        profileRepository.save(profile);
    }
}
