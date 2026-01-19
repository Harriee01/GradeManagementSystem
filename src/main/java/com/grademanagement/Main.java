package com.grademanagement;

import com.grademanagement.service.*;
import com.grademanagement.repository.*;
import com.grademanagement.utils.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Advanced Grade Management System ===");

        // Initialize services
        ValidationService validationService = new ValidationService();
        StudentRepository studentRepository = new StudentRepository();
        GradeRepository gradeRepository = new GradeRepository();
        AuditRepository auditRepository = new AuditRepository();

        StudentService studentService = new StudentService(studentRepository, validationService);
        GradeService gradeService = new GradeService(gradeRepository, validationService);
        StatisticsService statisticsService = new StatisticsService();
        FileService fileService = new FileService(validationService);
        CacheService cacheService = new CacheService();
        TaskScheduler taskScheduler = new TaskScheduler();
        PerformanceMonitor performanceMonitor = new PerformanceMonitor();

        // Start background services
        performanceMonitor.startMonitoring();
        taskScheduler.scheduleDailyGPACalculation();

        // Initialize application
        AdvancedGradeManagementSystem app = new AdvancedGradeManagementSystem(
               studentService,
               gradeService,
               statisticsService,
            fileService,
              cacheService,
               taskScheduler,
               performanceMonitor,
                validationService,
               auditRepository
       );

        app.run();

        // Shutdown services
        taskScheduler.shutdown();
        performanceMonitor.stopMonitoring();

        System.out.println("System shutdown complete.");
    }
}