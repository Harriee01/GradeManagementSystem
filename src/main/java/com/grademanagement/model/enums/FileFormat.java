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

    public String getDescription() {
        return description;
    }

    public static FileFormat fromExtension(String ext) {
        for (FileFormat format : values()) {
            if (format.extension.equalsIgnoreCase(ext)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unsupported format: " + ext);
    }

    // Returns the FileFormat based on the user's menu selection.
    public static FileFormat fromChoice(int choice) {
        return switch (choice) {
            case 1 -> CSV;
            case 2 -> JSON;
            case 3 -> BINARY;
            default -> throw new IllegalArgumentException("Invalid choice: " + choice);
        };
    }
}
