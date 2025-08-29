package com.intervale.diagnostictool.service;

import com.intervale.diagnostictool.dto.DiagnosticMethodFaultDto;
import com.intervale.diagnostictool.dto.request.DiagnosticMethodFaultRequest;
import com.intervale.diagnostictool.exception.ResourceAlreadyExistsException;
import com.intervale.diagnostictool.exception.ResourceNotFoundException;
import com.intervale.diagnostictool.mapper.FaultMapper;
import com.intervale.diagnostictool.model.DiagnosticMethod;
import com.intervale.diagnostictool.model.DiagnosticMethodFault;
import com.intervale.diagnostictool.model.FaultType;
import com.intervale.diagnostictool.model.enums.CoverageLevel;
import com.intervale.diagnostictool.repository.DiagnosticMethodFaultRepository;
import com.intervale.diagnostictool.repository.DiagnosticMethodRepository;
import com.intervale.diagnostictool.repository.FaultTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiagnosticMethodFaultService {

    private final DiagnosticMethodFaultRepository diagnosticMethodFaultRepository;
    private final DiagnosticMethodRepository diagnosticMethodRepository;
    private final FaultTypeRepository faultTypeRepository;
    private final FaultMapper faultMapper;

    @Transactional(readOnly = true)
    public List<DiagnosticMethodFaultDto> findByDiagnosticMethodId(Long methodId) {
        return faultMapper.toDiagnosticMethodFaultDtoList(
                diagnosticMethodFaultRepository.findByDiagnosticMethodId(methodId));
    }

    @Transactional(readOnly = true)
    public List<DiagnosticMethodFaultDto> findByFaultTypeId(Long faultTypeId) {
        return faultMapper.toDiagnosticMethodFaultDtoList(
                diagnosticMethodFaultRepository.findByFaultTypeId(faultTypeId));
    }

    @Transactional(readOnly = true)
    public DiagnosticMethodFaultDto findById(Long id) {
        return faultMapper.toDto(diagnosticMethodFaultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticMethodFault", "id", id)));
    }

    @Transactional
    public DiagnosticMethodFaultDto create(DiagnosticMethodFaultRequest request) {
        // Check if relationship already exists
        if (diagnosticMethodFaultRepository.findByDiagnosticMethodIdAndFaultTypeId(
                request.getDiagnosticMethodId(), request.getFaultTypeId()).isPresent()) {
            throw new ResourceAlreadyExistsException("DiagnosticMethodFault", 
                    "diagnosticMethodId and faultTypeId", 
                    request.getDiagnosticMethodId() + ", " + request.getFaultTypeId());
        }

        DiagnosticMethod diagnosticMethod = diagnosticMethodRepository.findById(request.getDiagnosticMethodId())
                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticMethod", "id", request.getDiagnosticMethodId()));
        
        FaultType faultType = faultTypeRepository.findById(request.getFaultTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("FaultType", "id", request.getFaultTypeId()));

        // Ensure the method and fault type belong to the same device category
        if (!diagnosticMethod.getDeviceCategory().getId().equals(faultType.getDeviceCategory().getId())) {
            throw new IllegalArgumentException("Diagnostic method and fault type must belong to the same device category");
        }

        DiagnosticMethodFault dmf = faultMapper.toEntity(diagnosticMethod, faultType, request.getEffectiveness());
        return faultMapper.toDto(diagnosticMethodFaultRepository.save(dmf));
    }

    @Transactional
    public DiagnosticMethodFaultDto update(Long id, CoverageLevel effectiveness) {
        DiagnosticMethodFault dmf = diagnosticMethodFaultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DiagnosticMethodFault", "id", id));
        
        dmf.setEffectiveness(effectiveness);
        return faultMapper.toDto(diagnosticMethodFaultRepository.save(dmf));
    }

    @Transactional
    public void delete(Long id) {
        if (!diagnosticMethodFaultRepository.existsById(id)) {
            throw new ResourceNotFoundException("DiagnosticMethodFault", "id", id);
        }
        diagnosticMethodFaultRepository.deleteById(id);
    }

    @Transactional
    public void deleteByDiagnosticMethodIdAndFaultTypeId(Long methodId, Long faultTypeId) {
        diagnosticMethodFaultRepository.deleteByDiagnosticMethodIdAndFaultTypeId(methodId, faultTypeId);
    }

    @Transactional(readOnly = true)
    public boolean existsByDiagnosticMethodIdAndFaultTypeId(Long methodId, Long faultTypeId) {
        return diagnosticMethodFaultRepository.findByDiagnosticMethodIdAndFaultTypeId(methodId, faultTypeId).isPresent();
    }

    @Transactional
    public List<DiagnosticMethodFaultDto> updateEffectivenessForFaultType(Long faultTypeId, CoverageLevel effectiveness) {
        List<DiagnosticMethodFault> dmfs = diagnosticMethodFaultRepository.findByFaultTypeId(faultTypeId);
        dmfs.forEach(dmf -> dmf.setEffectiveness(effectiveness));
        return faultMapper.toDiagnosticMethodFaultDtoList(
                diagnosticMethodFaultRepository.saveAll(dmfs));
    }

    @Transactional(readOnly = true)
    public List<DiagnosticMethodFaultDto> findByDiagnosticMethodIds(List<Long> methodIds) {
        return faultMapper.toDiagnosticMethodFaultDtoList(
                diagnosticMethodFaultRepository.findByDiagnosticMethodIds(methodIds));
    }
}
