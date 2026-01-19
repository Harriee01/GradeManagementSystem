package com.grademanagement.model.enums;

// Enum for supported file formats
public enum FileFormat {
    CSV("csv", "Comma Separated Values"),
    JSON("json", "JavaScript Object Notation"),
    BINARY("bin", "Binary Format");

    private final String extension;
    private final String description;

    FileFormat(String extension, String description) {
        this.extension = extension;
        this.description = description;
    }

    public String getExtension() {
        return extension;
    }

    public static FileFormat fromExtension(String ext) {
        for (FileFormat format : values()) {
            if (format.extension.equalsIgnoreCase(ext)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unsupported format: " + ext);
    }
}
