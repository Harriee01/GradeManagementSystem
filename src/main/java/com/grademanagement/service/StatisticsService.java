package com.grademanagement.service;

import com.grademanagement.model.Grade;
import com.grademanagement.model.Student;
import java.util.*;
import java.util.stream.Collectors;

// Service for statistical calculations - follows Single Responsibility Principle
public class StatisticsService {

    // Calculate class average
    public double calculateClassAverage(List<Student> students) {
        if (students == null || students.isEmpty()) {
            return 0.0;
        }

        double total = 0;
        int count = 0;

        for (Student student : students) {
            for (Grade grade : student.getGrades()) {
                total += grade.getScore();
                count++;
            }
        }

        return count > 0 ? total / count : 0.0;
    }

    // Find highest grade in class
    public double findHighestGrade(List<Student> students) {
        if (students == null || students.isEmpty()) {
            return 0.0;
        }

        double highest = Double.MIN_VALUE;

        for (Student student : students) {
            for (Grade grade : student.getGrades()) {
                if (grade.getScore() > highest) {
                    highest = grade.getScore();
                }
            }
        }

        return highest == Double.MIN_VALUE ? 0.0 : highest;
    }

    // Find lowest grade in class
    public double findLowestGrade(List<Student> students) {
        if (students == null || students.isEmpty()) {
            return 0.0;
        }

        double lowest = Double.MAX_VALUE;

        for (Student student : students) {
            for (Grade grade : student.getGrades()) {
                if (grade.getScore() < lowest) {
                    lowest = grade.getScore();
                }
            }
        }

        return lowest == Double.MAX_VALUE ? 0.0 : lowest;
    }

    // Calculate median grade
    public double calculateMedianGrade(List<Student> students) {
        List<Double> allGrades = new ArrayList<>();

        if (students == null) {
            return 0.0;
        }

        // Collect all grades
        for (Student student : students) {
            for (Grade grade : student.getGrades()) {
                allGrades.add(grade.getScore());
            }
        }

        if (allGrades.isEmpty()) {
            return 0.0;
        }

        // Sort grades
        Collections.sort(allGrades);

        int size = allGrades.size();
        if (size % 2 == 0) {
            // Even number of grades: average of two middle values
            return (allGrades.get(size/2 - 1) + allGrades.get(size/2)) / 2.0;
        } else {
            // Odd number of grades: middle value
            return allGrades.get(size/2);
        }
    }

    // Calculate standard deviation
    public double calculateStandardDeviation(List<Student> students) {
        double mean = calculateClassAverage(students);
        double sumSquaredDifferences = 0;
        int count = 0;

        if (students == null) {
            return 0.0;
        }

        for (Student student : students) {
            for (Grade grade : student.getGrades()) {
                double difference = grade.getScore() - mean;
                sumSquaredDifferences += difference * difference;
                count++;
            }
        }

        if (count <= 1) {
            return 0.0;
        }

        double variance = sumSquaredDifferences / (count - 1);
        return Math.sqrt(variance);
    }

    // Calculate mode (most frequent grade range)
    public String calculateGradeMode(List<Student> students) {
        if (students == null || students.isEmpty()) {
            return "N/A";
        }

        Map<String, Integer> gradeRangeCount = new HashMap<>();
        String[] ranges = {"90-100", "80-89", "70-79", "60-69", "50-59", "0-49"};

        // Initialize all ranges with 0
        for (String range : ranges) {
            gradeRangeCount.put(range, 0);
        }

        // Count grades in each range
        for (Student student : students) {
            for (Grade grade : student.getGrades()) {
                double score = grade.getScore();

                if (score >= 90) gradeRangeCount.put("90-100", gradeRangeCount.get("90-100") + 1);
                else if (score >= 80) gradeRangeCount.put("80-89", gradeRangeCount.get("80-89") + 1);
                else if (score >= 70) gradeRangeCount.put("70-79", gradeRangeCount.get("70-79") + 1);
                else if (score >= 60) gradeRangeCount.put("60-69", gradeRangeCount.get("60-69") + 1);
                else if (score >= 50) gradeRangeCount.put("50-59", gradeRangeCount.get("50-59") + 1);
                else gradeRangeCount.put("0-49", gradeRangeCount.get("0-49") + 1);
            }
        }

        // Find mode
        String mode = ranges[0];
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : gradeRangeCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mode = entry.getKey();
            }
        }

        return mode;
    }

    // Calculate pass rate for the class
    public double calculateClassPassRate(List<Student> students) {
        if (students == null || students.isEmpty()) {
            return 0.0;
        }

        int totalGrades = 0;
        int passingGrades = 0;

        for (Student student : students) {
            for (Grade grade : student.getGrades()) {
                totalGrades++;
                if (grade.isPassing()) {
                    passingGrades++;
                }
            }
        }

        return totalGrades > 0 ? (passingGrades * 100.0) / totalGrades : 0.0;
    }

    // Calculate average GPA for the class
    public double calculateClassAverageGPA(List<Student> students) {
        if (students == null || students.isEmpty()) {
            return 0.0;
        }

        double totalGPA = 0;
        int count = 0;

        for (Student student : students) {
            double studentGPA = student.calculateGPA();
            if (studentGPA > 0) { // Only count students with grades
                totalGPA += studentGPA;
                count++;
            }
        }

        return count > 0 ? totalGPA / count : 0.0;
    }

    // Calculate subject-wise averages
    public Map<String, Double> calculateSubjectAverages(List<Student> students) {
        Map<String, Double> subjectAverages = new HashMap<>();
        Map<String, Double> subjectSums = new HashMap<>();
        Map<String, Integer> subjectCounts = new HashMap<>();

        if (students == null) {
            return subjectAverages;
        }

        // Calculate sums and counts for each subject
        for (Student student : students) {
            for (Grade grade : student.getGrades()) {
                String subject = grade.getSubject().getName();
                double score = grade.getScore();

                subjectSums.put(subject, subjectSums.getOrDefault(subject, 0.0) + score);
                subjectCounts.put(subject, subjectCounts.getOrDefault(subject, 0) + 1);
            }
        }

        // Calculate averages
        for (Map.Entry<String, Double> entry : subjectSums.entrySet()) {
            String subject = entry.getKey();
            double sum = entry.getValue();
            int count = subjectCounts.get(subject);

            if (count > 0) {
                subjectAverages.put(subject, sum / count);
            }
        }

        return subjectAverages;
    }

    // Calculate grade distribution
    public Map<String, Integer> calculateGradeDistribution(List<Student> students) {
        Map<String, Integer> distribution = new HashMap<>();

        if (students == null) {
            return distribution;
        }

        // Initialize with all possible letter grades
        String[] letters = {"A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F"};
        for (String letter : letters) {
            distribution.put(letter, 0);
        }

        // Count actual grades
        for (Student student : students) {
            for (Grade grade : student.getGrades()) {
                String letter = grade.getLetterGrade().getLetter();
                distribution.put(letter, distribution.get(letter) + 1);
            }
        }

        return distribution;
    }

    // Calculate student ranking
    public Map<Student, Integer> calculateStudentRanking(List<Student> students) {
        Map<Student, Integer> ranking = new HashMap<>();

        if (students == null || students.isEmpty()) {
            return ranking;
        }

        // Filter students with grades
        List<Student> studentsWithGrades = students.stream()
                .filter(student -> !student.getGrades().isEmpty())
                .collect(Collectors.toList());

        // Sort by average grade (descending)
        studentsWithGrades.sort((s1, s2) ->
                Double.compare(s2.calculateAverageGrade(), s1.calculateAverageGrade()));

        // Assign ranks (handle ties)
        int rank = 1;
        for (int i = 0; i < studentsWithGrades.size(); i++) {
            Student current = studentsWithGrades.get(i);

            // Check for ties
            if (i > 0) {
                Student previous = studentsWithGrades.get(i - 1);
                if (Math.abs(current.calculateAverageGrade() - previous.calculateAverageGrade()) < 0.01) {
                    // Same rank for tie
                    ranking.put(current, ranking.get(previous));
                } else {
                    ranking.put(current, rank);
                }
            } else {
                ranking.put(current, rank);
            }

            rank++;
        }

        return ranking;
    }

    // Generate comprehensive statistics report
    public String generateStatisticsReport(List<Student> students) {
        StringBuilder report = new StringBuilder();

        report.append("=== CLASS STATISTICS REPORT ===\n");
        report.append(String.format("Total Students: %d\n", students.size()));

        // Active students
        long activeStudents = students.stream()
                .filter(Student::isActive)
                .count();
        report.append(String.format("Active Students: %d\n", activeStudents));

        // Student type distribution
        long honorsCount = students.stream()
                .filter(student -> student instanceof com.grademanagement.model.HonorsStudent)
                .count();
        long regularCount = students.size() - honorsCount;
        report.append(String.format("Honors Students: %d\n", honorsCount));
        report.append(String.format("Regular Students: %d\n", regularCount));

        // Grade statistics
        report.append(String.format("Class Average: %.2f%%\n", calculateClassAverage(students)));
        report.append(String.format("Highest Grade: %.2f%%\n", findHighestGrade(students)));
        report.append(String.format("Lowest Grade: %.2f%%\n", findLowestGrade(students)));
        report.append(String.format("Median Grade: %.2f%%\n", calculateMedianGrade(students)));
        report.append(String.format("Standard Deviation: %.2f\n", calculateStandardDeviation(students)));
        report.append(String.format("Most Common Grade Range: %s\n", calculateGradeMode(students)));
        report.append(String.format("Class Pass Rate: %.1f%%\n", calculateClassPassRate(students)));
        report.append(String.format("Class Average GPA: %.2f\n", calculateClassAverageGPA(students)));

        // Subject-wise averages
        Map<String, Double> subjectAverages = calculateSubjectAverages(students);
        if (!subjectAverages.isEmpty()) {
            report.append("\n=== SUBJECT-WISE AVERAGES ===\n");
            for (Map.Entry<String, Double> entry : subjectAverages.entrySet()) {
                report.append(String.format("  %s: %.2f%%\n", entry.getKey(), entry.getValue()));
            }
        }

        // Grade distribution
        Map<String, Integer> gradeDistribution = calculateGradeDistribution(students);
        report.append("\n=== GRADE DISTRIBUTION ===\n");
        for (Map.Entry<String, Integer> entry : gradeDistribution.entrySet()) {
            if (entry.getValue() > 0) {
                report.append(String.format("  %s: %d\n", entry.getKey(), entry.getValue()));
            }
        }

        // Top performers
        Map<Student, Integer> rankings = calculateStudentRanking(students);
        if (!rankings.isEmpty()) {
            report.append("\n=== TOP 5 PERFORMERS ===\n");
            rankings.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .limit(5)
                    .forEach(entry -> {
                        Student student = entry.getKey();
                        report.append(String.format("  #%d: %s (%.2f%% avg)\n",
                                entry.getValue(), student.getName(), student.calculateAverageGrade()));
                    });
        }

        report.append("===============================\n");

        return report.toString();
    }

    // Generate real-time statistics (for dashboard)
    public Map<String, Object> generateRealTimeStatistics(List<Student> students) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalStudents", students.size());
        stats.put("classAverage", calculateClassAverage(students));
        stats.put("classPassRate", calculateClassPassRate(students));
        stats.put("classGPA", calculateClassAverageGPA(students));
        stats.put("gradeDistribution", calculateGradeDistribution(students));
        stats.put("subjectAverages", calculateSubjectAverages(students));
        stats.put("timestamp", new Date());

        return stats;
    }

    // Calculate correlation between two subjects
    public double calculateSubjectCorrelation(List<Student> students, String subject1, String subject2) {
        List<Double> grades1 = new ArrayList<>();
        List<Double> grades2 = new ArrayList<>();

        // Collect grades for both subjects from each student
        for (Student student : students) {
            double grade1 = -1, grade2 = -1;

            for (Grade grade : student.getGrades()) {
                if (grade.getSubject().getName().equals(subject1)) {
                    grade1 = grade.getScore();
                }
                if (grade.getSubject().getName().equals(subject2)) {
                    grade2 = grade.getScore();
                }
            }

            // Only add if student has both grades
            if (grade1 >= 0 && grade2 >= 0) {
                grades1.add(grade1);
                grades2.add(grade2);
            }
        }

        // Need at least 2 data points for correlation
        if (grades1.size() < 2) {
            return 0.0;
        }

        // Calculate Pearson correlation coefficient
        double sum1 = grades1.stream().mapToDouble(Double::doubleValue).sum();
        double sum2 = grades2.stream().mapToDouble(Double::doubleValue).sum();
        double sum1Sq = grades1.stream().mapToDouble(g -> g * g).sum();
        double sum2Sq = grades2.stream().mapToDouble(g -> g * g).sum();
        double sum12 = 0;

        for (int i = 0; i < grades1.size(); i++) {
            sum12 += grades1.get(i) * grades2.get(i);
        }

        int n = grades1.size();
        double numerator = n * sum12 - sum1 * sum2;
        double denominator = Math.sqrt((n * sum1Sq - sum1 * sum1) * (n * sum2Sq - sum2 * sum2));

        return denominator != 0 ? numerator / denominator : 0.0;
    }

    // Predict student performance based on historical data
    public Map<String, Double> predictStudentPerformance(Student student) {
        Map<String, Double> predictions = new HashMap<>();

        if (student.getGrades().isEmpty()) {
            return predictions;
        }

        // Simple prediction: average of existing grades for each subject
        Map<String, List<Double>> subjectGrades = new HashMap<>();

        for (Grade grade : student.getGrades()) {
            String subject = grade.getSubject().getName();
            subjectGrades.computeIfAbsent(subject, k -> new ArrayList<>())
                    .add(grade.getScore());
        }

        // Calculate average for each subject
        for (Map.Entry<String, List<Double>> entry : subjectGrades.entrySet()) {
            double average = entry.getValue().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            predictions.put(entry.getKey(), average);
        }

        return predictions;
    }
}
