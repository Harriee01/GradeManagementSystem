package com.grademanagement.service;

import com.grademanagement.model.Student;
import com.grademanagement.model.Grade;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class BatchService {
    private final ExecutorService executor;
    private final StudentService studentService;
    private final GradeService gradeService;

    public BatchService(int poolSize, StudentService studentService, GradeService gradeService) {
        this.executor = Executors.newFixedThreadPool(poolSize);
        this.studentService = studentService;
        this.gradeService = gradeService;
    }

    public List<Future<Student>> batchUpdateGPA(List<Student> students) {
        List<Future<Student>> futures = new ArrayList<>();
        for (Student student : students) {
            Callable<Student> task = () -> {
                // GPA is already calculated, just return student
                return student;
            };
            futures.add(executor.submit(task));
        }
        return futures;
    }

    public Map<String, Double> batchCalculateStatistics() {
        List<Student> students = studentService.getAllStudents();
        Map<String, Double> stats = new ConcurrentHashMap<>();

        // Calculate in parallel
        students.parallelStream().forEach(student -> {
            double gpa = student.calculateGPA();
            stats.put(student.getId(), gpa);
        });

        return stats;
    }

    public void shutdown() {
        executor.shutdown();
    }
}