package com.intervale.diagnostictool.service;

import com.intervale.diagnostictool.exception.InUseException;
import com.intervale.diagnostictool.exception.ResourceNotFoundException;
import com.intervale.diagnostictool.model.Device;
import com.intervale.diagnostictool.model.DeviceCategory;
import com.intervale.diagnostictool.model.DiagnosticMethod;
import com.intervale.diagnostictool.repository.DeviceCategoryRepository;
import com.intervale.diagnostictool.repository.DeviceRepository;
import com.intervale.diagnostictool.repository.DiagnosticMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceCategoryService {
    private final DeviceCategoryRepository categoryRepository;
    private final DeviceRepository deviceRepository;
    private final DiagnosticMethodRepository diagnosticMethodRepository;
    private final DeviceService deviceService;
    private final DiagnosticMethodService diagnosticMethodService;

    @Transactional(readOnly = true)
    public List<DeviceCategory> findAll() {
        return categoryRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<DeviceCategory> findAllWithDevices() {
        return categoryRepository.findAllWithDevices();
    }
    
    @Transactional(readOnly = true)
    public List<DeviceCategory> findAllWithDiagnosticMethods() {
        return categoryRepository.findAllWithDiagnosticMethods();
    }

    @Transactional(readOnly = true)
    public DeviceCategory findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }
    
    @Transactional(readOnly = true)
    public DeviceCategory findByIdWithDevices(Long id) {
        return categoryRepository.findByIdWithDevices(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }
    
    @Transactional(readOnly = true)
    public DeviceCategory findByIdWithDiagnosticMethods(Long id) {
        return categoryRepository.findByIdWithDiagnosticMethods(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }
    
    @Transactional(readOnly = true)
    public DeviceCategory findByIdWithDetails(Long id) {
        return categoryRepository.findByIdWithDevicesAndMethods(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Transactional
    public DeviceCategory create(DeviceCategory category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalStateException("Category with name " + category.getName() + " already exists");
        }
        return categoryRepository.save(category);
    }
    
    @Transactional
    public DeviceCategory createWithDevices(DeviceCategory category, Set<Device> devices) {
        DeviceCategory savedCategory = create(category);
        
        if (devices != null && !devices.isEmpty()) {
            // Create a final reference for use in lambda
            final DeviceCategory finalSavedCategory = savedCategory;
            
            // Set the category for all devices and save them
            devices.forEach(device -> {
                device.setCategory(finalSavedCategory);
                deviceRepository.save(device);
            });
            
            // Update the category with the devices and save
            savedCategory.setDevices(devices);
            return categoryRepository.save(savedCategory);
        }
        
        return savedCategory;
    }

    @Transactional
    public DeviceCategory update(Long id, DeviceCategory categoryDetails) {
        DeviceCategory category = findById(id);
        
        if (!category.getName().equals(categoryDetails.getName()) && 
            categoryRepository.existsByName(categoryDetails.getName())) {
            throw new IllegalStateException("Category with name " + categoryDetails.getName() + " already exists");
        }
        
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        DeviceCategory category = findById(id);
        
        // Check if category has any devices
        if (deviceRepository.countByCategory(category) > 0) {
            throw new InUseException("Cannot delete category: it contains one or more devices");
        }
        
        // Check if category has any diagnostic methods
        if (diagnosticMethodRepository.countByDeviceCategory(category) > 0) {
            throw new InUseException("Cannot delete category: it contains one or more diagnostic methods");
        }
        
        categoryRepository.delete(category);
    }
    
    @Transactional(readOnly = true)
    public List<Device> getDevicesInCategory(Long categoryId) {
        return deviceRepository.findByCategoryId(categoryId);
    }
    
    @Transactional(readOnly = true)
    public List<DiagnosticMethod> getDiagnosticMethodsForCategory(Long categoryId) {
        return diagnosticMethodRepository.findByDeviceCategoryId(categoryId);
    }
    
    @Transactional
    public DeviceCategory updateCategoryDevices(Long categoryId, Set<Long> deviceIds) {
        DeviceCategory category = findById(categoryId);
        
        // Get current devices in this category
        List<Device> currentDevices = deviceRepository.findByCategory(category);
        Set<Long> currentDeviceIds = currentDevices.stream()
                .map(Device::getId)
                .collect(Collectors.toSet());
        
        // Find devices to add and remove
        Set<Long> deviceIdsToAdd = deviceIds.stream()
                .filter(id -> !currentDeviceIds.contains(id))
                .collect(Collectors.toSet());
                
        Set<Long> deviceIdsToRemove = currentDeviceIds.stream()
                .filter(id -> !deviceIds.contains(id))
                .collect(Collectors.toSet());
        
        // Add new devices to category
        if (!deviceIdsToAdd.isEmpty()) {
            List<Device> devicesToAdd = deviceRepository.findAllById(deviceIdsToAdd);
            devicesToAdd.forEach(device -> {
                device.setCategory(category);
                deviceRepository.save(device);
            });
        }
        
        // Remove devices from category
        if (!deviceIdsToRemove.isEmpty()) {
            List<Device> devicesToRemove = deviceRepository.findAllById(deviceIdsToRemove);
            devicesToRemove.forEach(device -> {
                device.setCategory(null);
                deviceRepository.save(device);
            });
        }
        
        return findByIdWithDevices(categoryId);
    }
    
    @Transactional
    public DeviceCategory updateCategoryDiagnosticMethods(Long categoryId, Set<Long> methodIds) {
        DeviceCategory category = findById(categoryId);
        
        // Get current methods for this category
        List<DiagnosticMethod> currentMethods = diagnosticMethodRepository.findByDeviceCategory(category);
        Set<Long> currentMethodIds = currentMethods.stream()
                .map(DiagnosticMethod::getId)
                .collect(Collectors.toSet());
        
        // Find methods to add and remove
        Set<Long> methodIdsToAdd = methodIds.stream()
                .filter(id -> !currentMethodIds.contains(id))
                .collect(Collectors.toSet());
                
        Set<Long> methodIdsToRemove = currentMethodIds.stream()
                .filter(id -> !methodIds.contains(id))
                .collect(Collectors.toSet());
        
        // Add new methods to category
        if (!methodIdsToAdd.isEmpty()) {
            List<DiagnosticMethod> methodsToAdd = diagnosticMethodRepository.findAllById(methodIdsToAdd);
            methodsToAdd.forEach(method -> {
                method.setDeviceCategory(category);
                diagnosticMethodRepository.save(method);
            });
        }
        
        // Remove methods from category
        if (!methodIdsToRemove.isEmpty()) {
            List<DiagnosticMethod> methodsToRemove = diagnosticMethodRepository.findAllById(methodIdsToRemove);
            methodsToRemove.forEach(method -> {
                // Check if method is used by any devices
                if (!method.getDevices().isEmpty()) {
                    throw new InUseException("Cannot remove diagnostic method: it is used by one or more devices");
                }
                diagnosticMethodRepository.delete(method);
            });
        }
        
        return findByIdWithDiagnosticMethods(categoryId);
    }
}
