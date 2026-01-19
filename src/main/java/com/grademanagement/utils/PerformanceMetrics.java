package com.grademanagement.utils;

import java.util.HashMap;
import java.util.Map;

public class PerformanceMetrics {
    private final Map<String, Long> startTimes = new HashMap<>();
    private final Map<String, Long> executionTimes = new HashMap<>();

    public void startTimer(String operation) {
        startTimes.put(operation, System.currentTimeMillis());
    }

    public void stopTimer(String operation) {
        Long startTime = startTimes.get(operation);
        if (startTime != null) {
            long executionTime = System.currentTimeMillis() - startTime;
            executionTimes.put(operation, executionTime);
        }
    }

    public long getExecutionTime(String operation) {
        return executionTimes.getOrDefault(operation, 0L);
    }

    public void printMetrics() {
        System.out.println("=== PERFORMANCE METRICS ===");
        for (Map.Entry<String, Long> entry : executionTimes.entrySet()) {
            System.out.printf("%s: %d ms\n", entry.getKey(), entry.getValue());
        }
    }
}