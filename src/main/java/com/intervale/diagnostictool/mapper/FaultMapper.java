package com.intervale.diagnostictool.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intervale.diagnostictool.dto.*;
import com.intervale.diagnostictool.dto.request.FaultTypeRequest;
import com.intervale.diagnostictool.dto.request.ProfileFaultRequest;
import com.intervale.diagnostictool.model.*;
import com.intervale.diagnostictool.model.enums.CoverageLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FaultMapper {

    private final ObjectMapper objectMapper;

    // FaultType mappings
    public FaultType toEntity(FaultTypeRequest request) {
        FaultType faultType = new FaultType(
                request.getCode(),
                request.getName(),
                request.getDescription(),
                null, // deviceCategory should be set separately as it's a relationship
                request.getCoverageRequirement(),
                request.getGostReference()
        );
        return faultType;
    }

    public FaultTypeDto toDto(FaultType entity) {
        return FaultTypeDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .deviceCategoryId(entity.getDeviceCategory() != null ? entity.getDeviceCategory().getId() : null)
                .deviceCategoryName(entity.getDeviceCategory() != null ? entity.getDeviceCategory().getName() : null)
                .coverageRequirement(entity.getCoverageRequirement())
                .gostReference(entity.getGostReference())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    // ProfileFault mappings
    public ProfileFault toEntity(ProfileFaultRequest request) {
        ProfileFault entity = new ProfileFault();
        entity.setCovered(request.isCovered());
        try {
            entity.setCoveredMethodsIds(request.getCoveredMethodIds() != null ? 
                    objectMapper.writeValueAsString(request.getCoveredMethodIds()) : null);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing covered method IDs", e);
        }
        entity.setNotes(request.getNotes());
        return entity;
    }

    public ProfileFaultDto toDto(ProfileFault entity) {
        ProfileFaultDto dto = new ProfileFaultDto();
        
        // Set IDs from the composite key if available
        if (entity.getId() != null) {
            dto.setId(entity.getId().getProfileId()); // Or another appropriate ID field
            dto.setProfileId(entity.getId().getProfileId());
            dto.setFaultTypeId(entity.getId().getFaultTypeId());
        }
        
        // Set fault type details
        if (entity.getFaultType() != null) {
            dto.setFaultTypeCode(entity.getFaultType().getCode());
            dto.setFaultTypeName(entity.getFaultType().getName());
            dto.setFaultTypeDescription(entity.getFaultType().getDescription());
            dto.setCoverageRequirement(entity.getFaultType().getCoverageRequirement().name());
            
            // Set device category name if available
            if (entity.getFaultType().getDeviceCategory() != null) {
                dto.setDeviceCategoryName(entity.getFaultType().getDeviceCategory().getName());
            }
        }
        
        dto.setCovered(entity.isCovered());
        dto.setCoveredMethodsIds(entity.getCoveredMethodsIds());
        dto.setNotes(entity.getNotes());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        // Parse covered methods IDs if present
        if (entity.getCoveredMethodsIds() != null && !entity.getCoveredMethodsIds().isEmpty()) {
            try {
                List<Long> methodIds = objectMapper.readValue(
                        entity.getCoveredMethodsIds(), 
                        new TypeReference<List<Long>>() {});
                dto.setCoveredMethods(methodIds);
            } catch (JsonProcessingException e) {
                dto.setCoveredMethods(Collections.emptyList());
            }
        } else {
            dto.setCoveredMethods(Collections.emptyList());
        }

        return dto;
    }

    // DiagnosticMethodFault mappings
    public DiagnosticMethodFault toEntity(DiagnosticMethod diagnosticMethod, FaultType faultType, CoverageLevel effectiveness) {
        DiagnosticMethodFault entity = new DiagnosticMethodFault();
        entity.setDiagnosticMethod(diagnosticMethod);
        entity.setFaultType(faultType);
        entity.setEffectiveness(effectiveness);
        return entity;
    }

    public DiagnosticMethodFaultDto toDto(DiagnosticMethodFault entity) {
        return DiagnosticMethodFaultDto.builder()
                .id(entity.getId())
                .diagnosticMethodId(entity.getDiagnosticMethod() != null ? entity.getDiagnosticMethod().getId() : null)
                .diagnosticMethodName(entity.getDiagnosticMethod() != null ? entity.getDiagnosticMethod().getName() : null)
                .diagnosticMethodDescription(entity.getDiagnosticMethod() != null ? 
                        entity.getDiagnosticMethod().getDescription() : null)
                .faultTypeId(entity.getFaultType() != null ? entity.getFaultType().getId() : null)
                .faultTypeCode(entity.getFaultType() != null ? entity.getFaultType().getCode() : null)
                .faultTypeName(entity.getFaultType() != null ? entity.getFaultType().getName() : null)
                .effectiveness(entity.getEffectiveness())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    // List conversions
    public List<FaultTypeDto> toFaultTypeDtoList(List<FaultType> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ProfileFaultDto> toProfileFaultDtoList(List<ProfileFault> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<DiagnosticMethodFaultDto> toDiagnosticMethodFaultDtoList(List<DiagnosticMethodFault> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
