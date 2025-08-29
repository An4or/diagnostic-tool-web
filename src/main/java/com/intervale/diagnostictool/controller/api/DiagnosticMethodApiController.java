package com.intervale.diagnostictool.controller.api;

import com.intervale.diagnostictool.model.Device;
import com.intervale.diagnostictool.model.DiagnosticMethod;
import com.intervale.diagnostictool.model.Device.ArchitectureType;
import com.intervale.diagnostictool.model.dto.DiagnosticMethodDTO;
import com.intervale.diagnostictool.service.DeviceService;
import com.intervale.diagnostictool.service.DiagnosticMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for diagnostic methods API.
 */
@RestController
@RequestMapping("/api/diagnostic-methods")
@RequiredArgsConstructor
public class DiagnosticMethodApiController {

    private final DiagnosticMethodService diagnosticMethodService;
    private final DeviceService deviceService;

    /**
     * Get all diagnostic methods available for a specific device and architecture.
     *
     * @param deviceId the ID of the device
     * @param architecture the architecture type (1oo1, 1oo2, 2oo3)
     * @return list of available diagnostic methods
     */
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<DiagnosticMethodDTO>> getMethodsForDeviceAndArchitecture(
            @PathVariable Long deviceId,
            @RequestParam("architecture") ArchitectureType architecture) {
        
        Device device = deviceService.findById(deviceId);
        if (device == null) {
            return ResponseEntity.notFound().build();
        }

        List<DiagnosticMethod> methods = diagnosticMethodService.findByDeviceAndArchitecture(device, architecture);
        List<DiagnosticMethodDTO> dtos = methods.stream()
                .map(DiagnosticMethodDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Calculate the total diagnostic coverage for selected methods.
     *
     * @param methodIds list of diagnostic method IDs
     * @return calculated coverage percentage
     */
    @PostMapping("/calculate-coverage")
    public ResponseEntity<Double> calculateCoverage(@RequestBody List<Long> methodIds) {
        if (methodIds == null || methodIds.isEmpty()) {
            return ResponseEntity.badRequest().body(0.0);
        }

        double coverage = diagnosticMethodService.calculateTotalCoverage(methodIds);
        return ResponseEntity.ok(coverage);
    }
}
