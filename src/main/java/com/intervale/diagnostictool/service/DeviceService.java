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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final DeviceCategoryRepository categoryRepository;
    private final DiagnosticMethodRepository diagnosticMethodRepository;

    @Transactional(readOnly = true)
    public List<Device> findAll() {
        return deviceRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public List<Device> findAllWithDetails() {
        return deviceRepository.findAll().stream()
                .map(device -> deviceRepository.findByIdWithDetails(device.getId())
                        .orElse(device))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Device findById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
    }
    
    @Transactional(readOnly = true)
    public Device findByIdWithDetails(Long id) {
        return deviceRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
    }

    @Transactional
    public Device create(Device device, Long categoryId) {
        if (deviceRepository.existsByName(device.getName())) {
            throw new IllegalStateException("Device with name " + device.getName() + " already exists");
        }
        
        if (categoryId != null) {
            DeviceCategory category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
            device.setCategory(category);
        }
        
        // Set default values if not provided
        if (device.getChannelCount() == null) {
            device.setChannelCount(1);
        }
        if (device.getArchitectureType() == null) {
            device.setArchitectureType(ArchitectureType.ONE_OUT_OF_ONE);
        }
        
        return deviceRepository.save(device);
    }

    @Transactional
    public Device update(Long id, Device deviceDetails, Long categoryId) {
        Device device = findById(id);
        
        if (!device.getName().equals(deviceDetails.getName()) && 
            deviceRepository.existsByName(deviceDetails.getName())) {
            throw new IllegalStateException("Device with name " + deviceDetails.getName() + " already exists");
        }
        
        device.setName(deviceDetails.getName());
        device.setDescription(deviceDetails.getDescription());
        
        if (categoryId != null) {
            DeviceCategory category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
            device.setCategory(category);
        } else {
            device.setCategory(null);
        }
        
        return deviceRepository.save(device);
    }

    @Transactional
    public void delete(Long id) {
        Device device = findById(id);
        
        // Check if device is used in any profiles
        if (!device.getProfiles().isEmpty()) {
            throw new IllegalStateException("Cannot delete device: it is used in one or more profiles");
        }
        
        deviceRepository.delete(device);
    }
    
    @Transactional(readOnly = true)
    public List<Device> findByCategoryId(Long categoryId) {
        if (categoryId == null) {
            return deviceRepository.findByCategoryIsNull();
        }
        return deviceRepository.findByCategoryId(categoryId);
    }
    
    @Transactional
    public Device addDiagnosticMethod(Long deviceId, Long methodId) {
        Device device = findById(deviceId);
        DiagnosticMethod method = diagnosticMethodRepository.findById(methodId)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnostic method not found with id: " + methodId));
        
        // Check if the method is already added
        if (device.getDiagnosticMethods().stream()
                .anyMatch(dm -> dm.getId().equals(methodId))) {
            throw new IllegalStateException("Diagnostic method already added to this device");
        }
        
        device.getDiagnosticMethods().add(method);
        return deviceRepository.save(device);
    }
    
    @Transactional
    public void removeDiagnosticMethod(Long deviceId, Long methodId) {
        Device device = findById(deviceId);
        boolean removed = device.getDiagnosticMethods()
                .removeIf(method -> method.getId().equals(methodId));
        
        if (removed) {
            deviceRepository.save(device);
        }
    }
    
    @Transactional(readOnly = true)
    public List<DiagnosticMethod> getAvailableDiagnosticMethods(Long deviceId) {
        Device device = findById(deviceId);
        if (device.getCategory() == null) {
            return Collections.emptyList();
        }
        return diagnosticMethodRepository.findByDeviceCategory(device.getCategory());
    }
}
