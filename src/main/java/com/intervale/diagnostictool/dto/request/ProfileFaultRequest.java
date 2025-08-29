package com.intervale.diagnostictool.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileFaultRequest {
    
    @NotNull(message = "Profile ID is required")
    private Long profileId;
    
    @NotNull(message = "Fault type ID is required")
    private Long faultTypeId;
    
    private boolean isCovered;
    
    private List<Long> coveredMethodIds;
    
    private String notes;
}
