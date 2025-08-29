package com.intervale.diagnostictool.controller;

import com.intervale.diagnostictool.dto.DiagnosticMethodFaultDto;
import com.intervale.diagnostictool.dto.request.DiagnosticMethodFaultRequest;
import com.intervale.diagnostictool.model.enums.CoverageLevel;
import com.intervale.diagnostictool.service.DiagnosticMethodFaultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Diagnostic Method Fault Management", 
     description = "APIs for managing relationships between diagnostic methods and fault types")
public class DiagnosticMethodFaultController {

    private final DiagnosticMethodFaultService diagnosticMethodFaultService;

    @GetMapping("/api/diagnostic-methods/{methodId}/faults")
    @Operation(summary = "Get all faults for a diagnostic method")
    public ResponseEntity<List<DiagnosticMethodFaultDto>> getFaultsForDiagnosticMethod(
            @PathVariable Long methodId) {
        return ResponseEntity.ok(diagnosticMethodFaultService.findByDiagnosticMethodId(methodId));
    }

    @GetMapping("/api/fault-types/{faultTypeId}/methods")
    @Operation(summary = "Get all diagnostic methods for a fault type")
    public ResponseEntity<List<DiagnosticMethodFaultDto>> getMethodsForFaultType(
            @PathVariable Long faultTypeId) {
        return ResponseEntity.ok(diagnosticMethodFaultService.findByFaultTypeId(faultTypeId));
    }

    @PostMapping("/api/diagnostic-method-faults")
    @Operation(summary = "Create a relationship between a diagnostic method and a fault type")
    public ResponseEntity<DiagnosticMethodFaultDto> createDiagnosticMethodFault(
            @Valid @RequestBody DiagnosticMethodFaultRequest request) {
        return new ResponseEntity<>(
                diagnosticMethodFaultService.create(request), 
                HttpStatus.CREATED);
    }

    @PutMapping("/api/diagnostic-method-faults/{id}")
    @Operation(summary = "Update the effectiveness of a diagnostic method for a fault type")
    public ResponseEntity<DiagnosticMethodFaultDto> updateDiagnosticMethodFault(
            @PathVariable Long id,
            @RequestParam CoverageLevel effectiveness) {
        return ResponseEntity.ok(diagnosticMethodFaultService.update(id, effectiveness));
    }

    @DeleteMapping("/api/diagnostic-method-faults/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a relationship between a diagnostic method and a fault type")
    public void deleteDiagnosticMethodFault(@PathVariable Long id) {
        diagnosticMethodFaultService.delete(id);
    }

    @DeleteMapping("/api/diagnostic-methods/{methodId}/faults/{faultTypeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a specific relationship by method ID and fault type ID")
    public void deleteDiagnosticMethodFaultByMethodAndFault(
            @PathVariable Long methodId,
            @PathVariable Long faultTypeId) {
        diagnosticMethodFaultService.deleteByDiagnosticMethodIdAndFaultTypeId(methodId, faultTypeId);
    }

    @PutMapping("/api/fault-types/{faultTypeId}/effectiveness")
    @Operation(summary = "Update effectiveness for all diagnostic methods of a fault type")
    public ResponseEntity<List<DiagnosticMethodFaultDto>> updateEffectivenessForFaultType(
            @PathVariable Long faultTypeId,
            @RequestParam CoverageLevel effectiveness) {
        return ResponseEntity.ok(
                diagnosticMethodFaultService.updateEffectivenessForFaultType(faultTypeId, effectiveness));
    }

    @PostMapping("/api/diagnostic-methods/faults/batch")
    @Operation(summary = "Get diagnostic method faults for multiple method IDs")
    public ResponseEntity<List<DiagnosticMethodFaultDto>> getDiagnosticMethodFaultsByMethodIds(
            @RequestBody List<Long> methodIds) {
        return ResponseEntity.ok(diagnosticMethodFaultService.findByDiagnosticMethodIds(methodIds));
    }
}
