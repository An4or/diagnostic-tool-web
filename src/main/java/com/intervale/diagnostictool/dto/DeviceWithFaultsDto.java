package com.intervale.diagnostictool.dto;

import com.intervale.diagnostictool.model.Device;
import com.intervale.diagnostictool.model.DiagnosticMethod;
import lombok.Data;

import java.util.List;

@Data
public class DeviceWithFaultsDto {
    private Device device;
    private List<FaultWithStatusDto> faults;
    private List<DiagnosticMethod> diagnosticMethods;
}
