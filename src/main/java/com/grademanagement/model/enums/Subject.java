package com.grademanagement.model.enums;

// Enum defines fixed set of subjects with their types
public enum Subject {
    // Core subjects (mandatory)
    MATHEMATICS("Core"),
    ENGLISH("Core"),
    SCIENCE("Core"),

    // Elective subjects (optional)
    MUSIC("Elective"),
    ART("Elective"),
    PHYSICAL_EDUCATION("Elective");

    private final String type;  // Store subject type

    // Constructor for enum
    Subject(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
