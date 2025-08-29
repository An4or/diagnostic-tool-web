package com.intervale.diagnostictool.dto;

import lombok.Data;

@Data
public class FaultWithStatusDto {
    private FaultTypeDto faultType;
    private boolean covered;
    private String notes;
}
