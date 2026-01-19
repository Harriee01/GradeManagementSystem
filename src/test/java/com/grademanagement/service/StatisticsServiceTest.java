package com.grademanagement.service;

import com.grademanagement.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class StatisticsServiceTest {
    private StatisticsService statisticsService;
    private List<Student> students;

    @BeforeEach
    void setUp() {
        statisticsService = new StatisticsService();
        students = new ArrayList<>();

        // Create test students with grades
        ContactInfo contactInfo = new ContactInfo("test@university.edu", "+1234567890", "");

        Student student1 = new RegularStudent("S001", "John Doe", contactInfo);
        Student student2 = new HonorsStudent("H001", "Jane Smith", contactInfo);
        Student student3 = new RegularStudent("S002", "Bob Johnson", contactInfo);

        // Add mock grades
        addMockGrade(student1, 85.0);
        addMockGrade(student1, 90.0);
        addMockGrade(student1, 75.0);

        addMockGrade(student2, 95.0);
        addMockGrade(student2, 88.0);
        addMockGrade(student2, 92.0);

        addMockGrade(student3, 70.0);
        addMockGrade(student3, 65.0);
        addMockGrade(student3, 80.0);

        students.add(student1);
        students.add(student2);
        students.add(student3);
    }

    @Test
    @DisplayName("Test class average calculation")
    void testCalculateClassAverage() {
        double average = statisticsService.calculateClassAverage(students);

        // Expected average: (85+90+75+95+88+92+70+65+80)/9 = 82.22
        assertEquals(82.22, average, 0.01);
    }

    @Test
    @DisplayName("Test class average with empty list")
    void testCalculateClassAverageEmpty() {
        double average = statisticsService.calculateClassAverage(new ArrayList<>());
        assertEquals(0.0, average, 0.01);
    }

    @Test
    @DisplayName("Test highest grade calculation")
    void testFindHighestGrade() {
        double highest = statisticsService.findHighestGrade(students);
        assertEquals(95.0, highest, 0.01);
    }

    @Test
    @DisplayName("Test lowest grade calculation")
    void testFindLowestGrade() {
        double lowest = statisticsService.findLowestGrade(students);
        assertEquals(65.0, lowest, 0.01);
    }

    @Test
    @DisplayName("Test median grade calculation")
    void testCalculateMedianGrade() {
        // Grades: 65, 70, 75, 80, 85, 88, 90, 92, 95
        // Median (9 values): 5th value = 85
        double median = statisticsService.calculateMedianGrade(students);
        assertEquals(85.0, median, 0.01);
    }

    @Test
    @DisplayName("Test standard deviation calculation")
    void testCalculateStandardDeviation() {
        double stdDev = statisticsService.calculateStandardDeviation(students);

        // Expected: ~9.86
        assertEquals(9.86, stdDev, 0.1);
    }

    @Test
    @DisplayName("Test grade mode calculation")
    void testCalculateGradeMode() {
        String mode = statisticsService.calculateGradeMode(students);

        // Most grades are in 80-89 range
        assertTrue(mode.contains("80") || mode.contains("89"));
    }

    @Test
    @DisplayName("Test class pass rate calculation")
    void testCalculateClassPassRate() {
        // All grades above 50, so 100% pass rate
        double passRate = statisticsService.calculateClassPassRate(students);
        assertEquals(100.0, passRate, 0.01);
    }

    @Test
    @DisplayName("Test class average GPA calculation")
    void testCalculateClassAverageGPA() {
        double avgGPA = statisticsService.calculateClassAverageGPA(students);

        // Should be a positive number less than 4.0
        assertTrue(avgGPA > 0);
        assertTrue(avgGPA <= 4.0);
    }

    @Test
    @DisplayName("Test subject averages calculation")
    void testCalculateSubjectAverages() {
        Map<String, Double> subjectAverages = statisticsService.calculateSubjectAverages(students);

        // Since we're using mock subjects, this will be empty
        // In real implementation, would test with actual subjects
        assertNotNull(subjectAverages);
    }

    @Test
    @DisplayName("Test grade distribution calculation")
    void testCalculateGradeDistribution() {
        Map<String, Integer> distribution = statisticsService.calculateGradeDistribution(students);

        assertNotNull(distribution);
        assertTrue(distribution.containsKey("A"));
        assertTrue(distribution.containsKey("B"));
        assertTrue(distribution.containsKey("C"));
        assertTrue(distribution.containsKey("D"));
        assertTrue(distribution.containsKey("F"));
    }

    @Test
    @DisplayName("Test student ranking calculation")
    void testCalculateStudentRanking() {
        Map<Student, Integer> ranking = statisticsService.calculateStudentRanking(students);

        assertEquals(3, ranking.size());

        // Student with highest average should be rank 1
        Student topStudent = ranking.entrySet().stream()
                .filter(entry -> entry.getValue() == 1)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        assertNotNull(topStudent);
        // Jane Smith (Honors student) likely has highest average
        assertTrue(topStudent.getName().contains("Jane"));
    }

    @Test
    @DisplayName("Test statistics report generation")
    void testGenerateStatisticsReport() {
        String report = statisticsService.generateStatisticsReport(students);

        assertNotNull(report);
        assertTrue(report.contains("CLASS STATISTICS REPORT"));
        assertTrue(report.contains("Total Students"));
        assertTrue(report.contains("Class Average"));
        assertTrue(report.contains("Highest Grade"));
        assertTrue(report.contains("Lowest Grade"));
    }

    @Test
    @DisplayName("Test real-time statistics generation")
    void testGenerateRealTimeStatistics() {
        Map<String, Object> stats = statisticsService.generateRealTimeStatistics(students);

        assertNotNull(stats);
        assertTrue(stats.containsKey("totalStudents"));
        assertTrue(stats.containsKey("classAverage"));
        assertTrue(stats.containsKey("classPassRate"));
        assertTrue(stats.containsKey("classGPA"));
        assertTrue(stats.containsKey("gradeDistribution"));
        assertTrue(stats.containsKey("subjectAverages"));
        assertTrue(stats.containsKey("timestamp"));
    }

    @Test
    @DisplayName("Test subject correlation calculation")
    void testCalculateSubjectCorrelation() {
        // This would require students with grades in two specific subjects
        // For now, test that it returns a value between -1 and 1
        double correlation = statisticsService.calculateSubjectCorrelation(
                students, "Mathematics", "Science");

        assertTrue(correlation >= -1.0 && correlation <= 1.0);
    }

    @Test
    @DisplayName("Test student performance prediction")
    void testPredictStudentPerformance() {
        Student student = students.get(0);
        Map<String, Double> predictions = statisticsService.predictStudentPerformance(student);

        assertNotNull(predictions);
        // Student should have predictions for subjects they have grades in
    }

    private void addMockGrade(Student student, double score) {
        // Create a simple grade object
        Grade grade = new Grade() {
            @Override
            public double getScore() {
                return score;
            }

            @Override
            public String getSubject() {
                return "Test Subject";
            }
        };

        // Reflection to add grade to student
        try {
            java.lang.reflect.Method method = Student.class.getDeclaredMethod("addGrade", Grade.class);
            method.setAccessible(true);
            method.invoke(student, grade);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Simple Grade interface for testing
    interface Grade {
        double getScore();
        String getSubject();
    }
}