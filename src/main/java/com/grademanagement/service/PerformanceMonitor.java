package com.grademanagement.service;

import java.lang.management.*;
import java.util.*;

public class PerformanceMonitor {
    private final ThreadMXBean threadMXBean;
    private final OperatingSystemMXBean osMXBean;
    private final MemoryMXBean memoryMXBean;
    private long startTime;

    public PerformanceMonitor() {
        this.threadMXBean = ManagementFactory.getThreadMXBean();
        this.osMXBean = ManagementFactory.getOperatingSystemMXBean();
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.startTime = System.currentTimeMillis();
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Memory usage
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        metrics.put("memoryUsage", heapUsage.getUsed() / (1024 * 1024)); // MB

        // CPU usage
        if (osMXBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsMXBean =
                    (com.sun.management.OperatingSystemMXBean) osMXBean;
            metrics.put("cpuUsage", sunOsMXBean.getProcessCpuLoad() * 100);
        }

        // Thread info
        metrics.put("activeThreads", threadMXBean.getThreadCount());
        metrics.put("uptime", (System.currentTimeMillis() - startTime) / 1000);

        return metrics;
    }

    public void startMonitoring() {
        // Start monitoring in background
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    System.out.println("[PERF] " + getMetrics());
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    public void stopMonitoring() {
        // Cleanup if needed
    }
}
