package com.intervale.diagnostictool.util;

import com.intervale.diagnostictool.model.Device.ArchitectureType;
import org.springframework.stereotype.Component;

@Component
public class ArchitectureTypeFormatter {
    
    public String format(ArchitectureType type) {
        if (type == null) {
            return "";
        }
        
        switch (type) {
            case ONE_OUT_OF_ONE:
                return "1oo1";
            case ONE_OUT_OF_TWO:
                return "1oo2";
            case TWO_OUT_OF_THREE:
                return "2oo3";
            default:
                return type.name();
        }
    }
}
