package com.grademanagement.service;

import java.util.concurrent.*;
import java.time.LocalTime;
import java.util.*;

public class TaskScheduler {
    private final ScheduledExecutorService scheduler;
    private final Map<String, ScheduledFuture<?>> scheduledTasks;

    public TaskScheduler() {
        this.scheduler = Executors.newScheduledThreadPool(3);
        this.scheduledTasks = new ConcurrentHashMap<>();
    }

    public void scheduleDailyGPACalculation() {
        Runnable task = () -> System.out.println("[" + LocalTime.now() + "] GPA calculation running");
        long initialDelay = calculateInitialDelay(2, 0);
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(task, initialDelay, 24, TimeUnit.HOURS);
        scheduledTasks.put("daily_gpa_calculation", future);
    }

    private long calculateInitialDelay(int targetHour, int targetMinute) {
        Calendar now = Calendar.getInstance();
        Calendar target = (Calendar) now.clone();
        target.set(Calendar.HOUR_OF_DAY, targetHour);
        target.set(Calendar.MINUTE, targetMinute);
        target.set(Calendar.SECOND, 0);

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1);
        }
        return target.getTimeInMillis() - now.getTimeInMillis();
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}