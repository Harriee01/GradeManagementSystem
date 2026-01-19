package com.grademanagement.service;

import com.grademanagement.model.Student;
import com.grademanagement.model.enums.FileFormat;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.*;

public class ReportService {
    private final ExecutorService executorService;
    private final FileService fileService;
    private final CompletionService<Path> completionService;

    public ReportService(int threadPoolSize) {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.completionService = new ExecutorCompletionService<Path>(executorService);
        this.fileService = new FileService(new ValidationService());
    }

    public List<Path> generateBatchReports(List<Student> students, Path outputDir, FileFormat format) {
        List<Future<Path>> futures = new ArrayList<>();
        List<Path> generatedReports = new ArrayList<>();

        for (Student student : students) {
            Callable<Path> task = () -> generateStudentReport(student, outputDir, format);
            futures.add(completionService.submit(task));
        }

        for (int i = 0; i < students.size(); i++) {
            try {
                Future<Path> future = completionService.take();
                Path reportPath = future.get();
                generatedReports.add(reportPath);
            } catch (Exception e) {
                System.err.println("Failed to generate report: " + e.getMessage());
            }
        }
        return generatedReports;
    }

    private Path generateStudentReport(Student student, Path outputDir, FileFormat format) throws Exception {
        String filename = String.format("report_%s_%s.%s",
                student.getId(), System.currentTimeMillis(), format.getExtension());
        Path outputPath = outputDir.resolve(filename);
        fileService.exportStudentReport(student, outputPath.toString());
        return outputPath;
    }

    public void shutdown() {
        executorService.shutdown();
    }
}