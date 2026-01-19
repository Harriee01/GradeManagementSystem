package com.grademanagement.service;

import com.grademanagement.model.Student;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DashboardService {
    private final ScheduledExecutorService scheduler;
    private final StatisticsService statisticsService;
    private volatile boolean running = false;

    public DashboardService(StatisticsService statisticsService) {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.statisticsService = statisticsService;
    }

    public void start(List<Student> students) {
        if (running) return;
        running = true;

        scheduler.scheduleAtFixedRate(() -> {
            updateDashboard(students);
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void updateDashboard(List<Student> students) {
        double avg = statisticsService.calculateClassAverage(students);
        System.out.printf("[DASHBOARD] Students: %d | Avg: %.2f\n", students.size(), avg);
    }

    public void stop() {
        running = false;
        scheduler.shutdown();
    }
}