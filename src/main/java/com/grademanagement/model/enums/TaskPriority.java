package com.grademanagement.model.enums;


// Enum for task priorities in the scheduler
public enum TaskPriority {
    HIGH(3, "High Priority", 1000),    // Executes first
    MEDIUM(2, "Medium Priority", 2000), // Normal priority
    LOW(1, "Low Priority", 3000);       // Executes last

    private final int value;
    private final String description;
    private final long delayMillis;  // Delay before execution

    TaskPriority(int value, String description, long delayMillis) {
        this.value = value;
        this.description = description;
        this.delayMillis = delayMillis;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public long getDelayMillis() {
        return delayMillis;
    }

    // Returns the TaskPriority enum that matches the given integer value.
    public static TaskPriority fromValue(int value) {
        for (TaskPriority priority : values()) {
            if (priority.value == value) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Invalid priority value: " + value);
    }
}
