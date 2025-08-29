package com.intervale.diagnostictool.model.enums;

public enum CoverageLevel {
    LOW("Низкий"),
    MEDIUM("Средний"),
    HIGH("Высокий");

    private final String displayName;

    CoverageLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static CoverageLevel fromString(String value) {
        for (CoverageLevel level : CoverageLevel.values()) {
            if (level.name().equalsIgnoreCase(value)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown coverage level: " + value);
    }
}
