package com.intervale.diagnostictool.service;

import com.intervale.diagnostictool.exception.ResourceNotFoundException;
import com.intervale.diagnostictool.model.Device;
import com.intervale.diagnostictool.model.Device.ArchitectureType;
import com.intervale.diagnostictool.model.DeviceCategory;
import com.intervale.diagnostictool.model.DiagnosticMethod;
import com.intervale.diagnostictool.repository.DeviceCategoryRepository;
import com.intervale.diagnostictool.repository.DeviceRepository;
import com.intervale.diagnostictool.repository.DiagnosticMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiagnosticMethodService {
    
    private final DiagnosticMethodRepository diagnosticMethodRepository;
    private final DeviceCategoryRepository categoryRepository;
    private final DeviceRepository deviceRepository;
    
    @Transactional(readOnly = true)
    public List<DiagnosticMethod> findAll() {
        return diagnosticMethodRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public DiagnosticMethod findById(Long id) {
        return diagnosticMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnostic method not found with id: " + id));
    }
    
    @Transactional(readOnly = true)
    public List<DiagnosticMethod> findByCategoryId(Long categoryId) {
        return diagnosticMethodRepository.findByDeviceCategoryId(categoryId);
    }
    
    /**
     * Find all diagnostic methods suitable for a specific device and architecture.
     *
     * @param device the device
     * @param architecture the architecture type (1oo1, 1oo2, 2oo3)
     * @return list of suitable diagnostic methods
     */
    @Transactional(readOnly = true)
    public List<DiagnosticMethod> findByDeviceAndArchitecture(Device device, ArchitectureType architecture) {
        if (device == null || architecture == null) {
            throw new IllegalArgumentException("Device and architecture must not be null");
        }
        
        // Get all methods for the device
        Set<DiagnosticMethod> deviceMethods = device.getDiagnosticMethods();
        
        // Get all methods for the device's category
        List<DiagnosticMethod> categoryMethods = diagnosticMethodRepository
                .findByDeviceCategory(device.getCategory());
                
        // Combine both lists and get distinct methods
        Set<DiagnosticMethod> allMethods = new HashSet<>();
        allMethods.addAll(deviceMethods);
        allMethods.addAll(categoryMethods);
        
        // Filter methods by architecture suitability and sort by coverage percentage
        return allMethods.stream()
                .filter(method -> method.isSuitableForArchitecture(architecture))
                .sorted(DiagnosticMethod::compareByCoveragePercent)
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate the total diagnostic coverage for a list of method IDs.
     * Uses the formula: 1 - ∏(1 - DC_i) where DC_i is the coverage of each method.
     *
     * @param methodIds list of diagnostic method IDs
     * @return total coverage as a percentage (0.0 to 100.0)
     */
    @Transactional(readOnly = true)
    public double calculateTotalCoverage(List<Long> methodIds) {
        if (methodIds == null || methodIds.isEmpty()) {
            return 0.0;
        }
        
        // Get all methods and calculate the product of (1 - coverage/100)
        double product = 1.0;
        for (Long methodId : methodIds) {
            DiagnosticMethod method = findById(methodId);
            if (method != null && method.getDiagnosticCoverage() != null) {
                double coverage = method.getDiagnosticCoverage().doubleValue() / 100.0;
                product *= (1.0 - coverage);
            }
        }
        
        // Calculate total coverage: (1 - product) * 100
        double totalCoverage = (1.0 - product) * 100.0;
        
        // Round to 2 decimal places
        return Math.round(totalCoverage * 100.0) / 100.0;
    }
    
    @Transactional
    public DiagnosticMethod create(DiagnosticMethod method, Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID is required");
        }
        
        DeviceCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        
        if (diagnosticMethodRepository.existsByNameAndDeviceCategory(method.getName(), category)) {
            throw new IllegalStateException("Diagnostic method with this name already exists in this category");
        }
        
        method.setDeviceCategory(category);
        return diagnosticMethodRepository.save(method);
    }
    
    @Transactional
    public DiagnosticMethod update(Long id, DiagnosticMethod methodDetails, Long categoryId) {
        DiagnosticMethod method = findById(id);
        
        // Check if name is being changed and if the new name already exists in the category
        if (!method.getName().equals(methodDetails.getName())) {
            DeviceCategory category = categoryId != null ? 
                    categoryRepository.findById(categoryId).orElse(method.getDeviceCategory()) : 
                    method.getDeviceCategory();
                    
            if (diagnosticMethodRepository.existsByNameAndDeviceCategory(methodDetails.getName(), category)) {
                throw new IllegalStateException("Diagnostic method with this name already exists in this category");
            }
            method.setName(methodDetails.getName());
        }
        
        // Update category if changed
        if (categoryId != null && !categoryId.equals(method.getDeviceCategory().getId())) {
            DeviceCategory newCategory = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
            method.setDeviceCategory(newCategory);
        }
        
        // Update other fields
        method.setDescription(methodDetails.getDescription());
        method.setCoverageLevel(methodDetails.getCoverageLevel());
        method.setCoveragePercent(methodDetails.getCoveragePercent());
        method.setImplementationComplexity(methodDetails.getImplementationComplexity());
        method.setGostReference(methodDetails.getGostReference());
        
        return diagnosticMethodRepository.save(method);
    }
    
    @Transactional
    public void delete(Long id) {
        DiagnosticMethod method = findById(id);
        
        // Check if method is used by any devices
        if (!method.getDevices().isEmpty()) {
            throw new IllegalStateException("Cannot delete diagnostic method: it is used by one or more devices");
        }
        
        diagnosticMethodRepository.delete(method);
    }
    
    @Transactional(readOnly = true)
    public List<Device> getDevicesUsingMethod(Long methodId) {
        return deviceRepository.findByDiagnosticMethodsId(methodId);
    }
    
    @Transactional(readOnly = true)
    public List<DiagnosticMethod> getCompatibleMethods(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));
                
        if (device.getCategory() == null) {
            return List.of();
        }
        
        return diagnosticMethodRepository.findByDeviceCategory(device.getCategory());
    }
    
    @Transactional
    public void addMethodToDevice(Long methodId, Long deviceId) {
        DiagnosticMethod method = findById(methodId);
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));
                
        if (device.getCategory() == null || !device.getCategory().equals(method.getDeviceCategory())) {
            throw new IllegalStateException("Diagnostic method is not compatible with the device's category");
        }
        
        if (!device.getDiagnosticMethods().add(method)) {
            throw new IllegalStateException("Device already has this diagnostic method");
        }
        
        deviceRepository.save(device);
    }
    
    @Transactional
    public void removeMethodFromDevice(Long methodId, Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));
                
        boolean removed = device.getDiagnosticMethods()
                .removeIf(method -> method.getId().equals(methodId));
                
        if (removed) {
            deviceRepository.save(device);
        }
    }
}
