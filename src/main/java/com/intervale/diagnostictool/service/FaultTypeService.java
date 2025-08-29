package com.intervale.diagnostictool.service;

import com.intervale.diagnostictool.dto.FaultTypeDto;
import com.intervale.diagnostictool.dto.request.FaultTypeRequest;
import com.intervale.diagnostictool.exception.ResourceNotFoundException;
import com.intervale.diagnostictool.mapper.FaultMapper;
import com.intervale.diagnostictool.model.DeviceCategory;
import com.intervale.diagnostictool.model.FaultType;
import com.intervale.diagnostictool.repository.DeviceCategoryRepository;
import com.intervale.diagnostictool.repository.FaultTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FaultTypeService {

    private final FaultTypeRepository faultTypeRepository;
    private final DeviceCategoryRepository deviceCategoryRepository;
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
    public List<FaultTypeDto> findByDeviceCategoryId(Long categoryId) {
        return faultMapper.toFaultTypeDtoList(faultTypeRepository.findByDeviceCategoryId(categoryId));
    }

    @Transactional
    public FaultTypeDto create(FaultTypeRequest request) {
        DeviceCategory category = deviceCategoryRepository.findById(request.getDeviceCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("DeviceCategory", "id", request.getDeviceCategoryId()));
        
        FaultType faultType = faultMapper.toEntity(request);
        category.addFaultType(faultType);
        
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
        
        // Update device category if changed
        if (!existingFaultType.getDeviceCategory().getId().equals(request.getDeviceCategoryId())) {
            DeviceCategory newCategory = deviceCategoryRepository.findById(request.getDeviceCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("DeviceCategory", "id", request.getDeviceCategoryId()));
            
            existingFaultType.getDeviceCategory().getFaultTypes().remove(existingFaultType);
            newCategory.addFaultType(existingFaultType);
        }
        
        return faultMapper.toDto(faultTypeRepository.save(existingFaultType));
    }

    @Transactional
    public void delete(Long id) {
        FaultType faultType = findFaultTypeOrThrow(id);
        faultType.getDeviceCategory().getFaultTypes().remove(faultType);
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
