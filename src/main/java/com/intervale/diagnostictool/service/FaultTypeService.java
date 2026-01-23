package com.intervale.diagnostictool.service;

import com.intervale.diagnostictool.dto.FaultTypeDto;
import com.intervale.diagnostictool.dto.request.FaultTypeRequest;
import com.intervale.diagnostictool.exception.ResourceNotFoundException;
import com.intervale.diagnostictool.mapper.FaultMapper;
import com.intervale.diagnostictool.model.Device;
import com.intervale.diagnostictool.model.FaultType;
import com.intervale.diagnostictool.repository.DeviceRepository;
import com.intervale.diagnostictool.repository.FaultTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FaultTypeService {

    private final FaultTypeRepository faultTypeRepository;
    private final DeviceRepository deviceRepository;
    private final FaultMapper faultMapper;

    @Transactional(readOnly = true)
    public List<FaultTypeDto> findAll() {
        return faultMapper.toFaultTypeDtoList(faultTypeRepository.findAll());
    }

    @Transactional(readOnly = true)
    public FaultTypeDto findById(Long id) {
        return faultMapper.toDto(findFaultTypeOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<FaultTypeDto> findByDeviceId(Long deviceId) {
        return faultMapper.toFaultTypeDtoList(faultTypeRepository.findByDeviceId(deviceId));
    }


    @Transactional(readOnly = true)
    public List<FaultTypeDto> findByDeviceCategoryId(Long categoryId) {
        return faultMapper.toFaultTypeDtoList(faultTypeRepository.findByDeviceCategoryId(categoryId));
    }

    @Transactional
    public FaultTypeDto create(FaultTypeRequest request) {
        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", request.getDeviceId()));
        
        FaultType faultType = faultMapper.toEntity(request);
        faultType.setDevice(device);
        
        return faultMapper.toDto(faultTypeRepository.save(faultType));
    }

    @Transactional
    public FaultTypeDto update(Long id, FaultTypeRequest request) {
        FaultType existingFaultType = findFaultTypeOrThrow(id);
        
        // Update fields from request
        existingFaultType.setCode(request.getCode());
        existingFaultType.setName(request.getName());
        existingFaultType.setDescription(request.getDescription());
        existingFaultType.setCoverageRequirement(request.getCoverageRequirement());
        existingFaultType.setGostReference(request.getGostReference());
        
        // Update device if changed
        if (existingFaultType.getDevice() == null || !existingFaultType.getDevice().getId().equals(request.getDeviceId())) {
            Device newDevice = deviceRepository.findById(request.getDeviceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Device", "id", request.getDeviceId()));
            existingFaultType.setDevice(newDevice);
        }
        
        return faultMapper.toDto(faultTypeRepository.save(existingFaultType));
    }

    @Transactional
    public void delete(Long id) {
        FaultType faultType = findFaultTypeOrThrow(id);
        faultTypeRepository.delete(faultType);
    }

    @Transactional(readOnly = true)
    public List<FaultTypeDto> search(String query) {
        return faultMapper.toFaultTypeDtoList(faultTypeRepository.search(query));
    }

    private FaultType findFaultTypeOrThrow(Long id) {
        return faultTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FaultType", "id", id));
    }
}
