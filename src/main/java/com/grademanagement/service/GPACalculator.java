package com.grademanagement.service;

import com.grademanagement.model.Grade;
import com.grademanagement.model.Student;
import com.grademanagement.model.enums.GradeLetter;
import java.util.List;
import java.util.Map;

// Service class for GPA calculations - follows Single Responsibility Principle
public class GPACalculator {

    // Calculate cumulative GPA for a student
    public double calculateCumulativeGPA(Student student) {
        List<Grade> grades = student.getGrades();
        if (grades.isEmpty()) {
            return 0.0;
        }

        double totalGPA = grades.stream()
                .mapToDouble(Grade::getGPA)
                .sum();

        return totalGPA / grades.size();
    }

    // Calculate weighted GPA (core subjects weighted higher)
    public double calculateWeightedGPA(Student student) {
        List<Grade> grades = student.getGrades();
        if (grades.isEmpty()) {
            return 0.0;
        }

        double weightedSum = 0;
        int totalCredits = 0;

        for (Grade grade : grades) {
            int creditWeight = getCreditWeight(grade);
            weightedSum += grade.getGPA() * creditWeight;
            totalCredits += creditWeight;
        }

        return totalCredits > 0 ? weightedSum / totalCredits : 0.0;
    }

    // Get credit weight based on subject type
    private int getCreditWeight(Grade grade) {
        String subjectType = grade.getSubject().getSubjectType();

        if ("Core".equals(subjectType)) {
            return 4;  // Core subjects typically 4 credits
        } else if ("Elective".equals(subjectType)) {
            return 3;  // Electives typically 3 credits
        } else {
            return 3;  // Default
        }
    }

    // Convert percentage to letter grade
    public String getLetterGrade(double percentage) {
        return GradeLetter.fromPercentage(percentage).getLetter();
    }

    // Convert percentage to GPA (4.0 scale)
    public double percentageToGPA(double percentage) {
        return GradeLetter.fromPercentage(percentage).getGpaValue();
    }

    // Calculate semester GPA
    public double calculateSemesterGPA(Student student, String semester) {
        List<Grade> semesterGrades = student.getGradesBySemester(semester);
        if (semesterGrades.isEmpty()) {
            return 0.0;
        }

        double totalGPA = semesterGrades.stream()
                .mapToDouble(Grade::getGPA)
                .sum();

        return totalGPA / semesterGrades.size();
    }

    // Calculate GPA by subject type
    public Map<String, Double> calculateGPABySubjectType(Student student) {
        Map<String, Double> gpaByType = new java.util.HashMap<>();

        // Core subjects GPA
        List<Grade> coreGrades = student.getGradesBySubjectType("Core");
        if (!coreGrades.isEmpty()) {
            double coreGPA = coreGrades.stream()
                    .mapToDouble(Grade::getGPA)
                    .average()
                    .orElse(0.0);
            gpaByType.put("Core", coreGPA);
        }

        // Elective subjects GPA
        List<Grade> electiveGrades = student.getGradesBySubjectType("Elective");
        if (!electiveGrades.isEmpty()) {
            double electiveGPA = electiveGrades.stream()
                    .mapToDouble(Grade::getGPA)
                    .average()
                    .orElse(0.0);
            gpaByType.put("Elective", electiveGPA);
        }

        return gpaByType;
    }

    // Calculate GPA trend (improvement over time)
    public Map<String, Double> calculateGPATrend(Student student) {
        Map<String, Double> gpaTrend = new java.util.LinkedHashMap<>();

        // Group grades by semester and calculate GPA for each
        Map<String, List<Grade>> gradesBySemester = new java.util.HashMap<>();

        for (Grade grade : student.getGrades()) {
            String semester = grade.getSemester();
            if (semester != null && !semester.trim().isEmpty()) {
                gradesBySemester.computeIfAbsent(semester, k -> new java.util.ArrayList<>())
                        .add(grade);
            }
        }

        // Calculate GPA for each semester
        for (Map.Entry<String, List<Grade>> entry : gradesBySemester.entrySet()) {
            double semesterGPA = entry.getValue().stream()
                    .mapToDouble(Grade::getGPA)
                    .average()
                    .orElse(0.0);
            gpaTrend.put(entry.getKey(), semesterGPA);
        }

        return gpaTrend;
    }

    // Calculate what grade is needed to achieve target GPA
    public Map<String, Double> calculateGradesNeededForTarget(Student student,
                                                              double targetGPA,
                                                              int futureCredits) {
        Map<String, Double> results = new java.util.HashMap<>();

        double currentGPA = calculateCumulativeGPA(student);
        int currentCredits = student.getGrades().size() * 3; // Approximate

        if (currentCredits + futureCredits <= 0) {
            results.put("error", -1.0);
            return results;
        }

        // Calculate required future GPA
        double requiredFutureGPA = (targetGPA * (currentCredits + futureCredits) -
                currentGPA * currentCredits) / futureCredits;

        // Convert to percentage
        double requiredPercentage = gpaToPercentage(requiredFutureGPA);

        results.put("currentGPA", currentGPA);
        results.put("targetGPA", targetGPA);
        results.put("requiredFutureGPA", requiredFutureGPA);
        results.put("requiredPercentage", requiredPercentage);
//        results.put("achievable", requiredFutureGPA <= 4.0);

        return results;
    }

    // Convert GPA back to approximate percentage
    private double gpaToPercentage(double gpa) {
        if (gpa >= 4.0) return 93;
        else if (gpa >= 3.7) return 90;
        else if (gpa >= 3.3) return 87;
        else if (gpa >= 3.0) return 83;
        else if (gpa >= 2.7) return 80;
        else if (gpa >= 2.3) return 77;
        else if (gpa >= 2.0) return 73;
        else if (gpa >= 1.7) return 70;
        else if (gpa >= 1.3) return 67;
        else if (gpa >= 1.0) return 65;
        else return 0;
    }

    // Calculate class rank based on GPA
    public int calculateClassRank(Student student, List<Student> allStudents) {
        if (allStudents.isEmpty()) {
            return 1;
        }

        // Calculate GPAs for all students
        Map<Student, Double> studentGPAs = new java.util.HashMap<>();
        for (Student s : allStudents) {
            if (!s.getGrades().isEmpty()) {
                studentGPAs.put(s, calculateCumulativeGPA(s));
            }
        }

        // Sort by GPA descending
        List<Map.Entry<Student, Double>> sorted = new java.util.ArrayList<>(studentGPAs.entrySet());
        sorted.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

        // Find rank
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getKey().equals(student)) {
                return i + 1;
            }
        }

        return sorted.size() + 1; // If not found, rank last
    }

    // Calculate GPA with grade replacement (replace lowest grade)
    public double calculateGPAWithReplacement(Student student, Grade replacementGrade) {
        List<Grade> grades = new java.util.ArrayList<>(student.getGrades());

        if (grades.isEmpty()) {
            return replacementGrade.getGPA();
        }

        // Find lowest grade
        Grade lowestGrade = grades.stream()
                .min(java.util.Comparator.comparingDouble(Grade::getGPA))
                .orElse(null);

        // Replace if replacement is better
        if (lowestGrade != null && replacementGrade.getGPA() > lowestGrade.getGPA()) {
            grades.remove(lowestGrade);
            grades.add(replacementGrade);
        }

        // Calculate new GPA
        double totalGPA = grades.stream()
                .mapToDouble(Grade::getGPA)
                .sum();

        return totalGPA / grades.size();
    }

    // Generate GPA report for student
    public String generateGPAReport(Student student) {
        StringBuilder report = new StringBuilder();

        report.append("=== GPA ANALYSIS REPORT ===\n");
        report.append(String.format("Student: %s (%s)\n", student.getName(), student.getId()));

        double cumulativeGPA = calculateCumulativeGPA(student);
        double weightedGPA = calculateWeightedGPA(student);

        report.append(String.format("Cumulative GPA: %.3f\n", cumulativeGPA));
        report.append(String.format("Weighted GPA: %.3f\n", weightedGPA));

        // GPA by subject type
        Map<String, Double> gpaByType = calculateGPABySubjectType(student);
        if (!gpaByType.isEmpty()) {
            report.append("\nGPA by Subject Type:\n");
            for (Map.Entry<String, Double> entry : gpaByType.entrySet()) {
                report.append(String.format("  %s: %.3f\n", entry.getKey(), entry.getValue()));
            }
        }

        // GPA trend
        Map<String, Double> gpaTrend = calculateGPATrend(student);
        if (!gpaTrend.isEmpty()) {
            report.append("\nGPA Trend by Semester:\n");
            for (Map.Entry<String, Double> entry : gpaTrend.entrySet()) {
                report.append(String.format("  %s: %.3f\n", entry.getKey(), entry.getValue()));
            }
        }

        // Letter grade distribution
        Map<String, Long> gradeDistribution = student.getGrades().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        grade -> grade.getLetterGrade().getLetter(),
                        java.util.stream.Collectors.counting()
                ));

        if (!gradeDistribution.isEmpty()) {
            report.append("\nLetter Grade Distribution:\n");
            for (Map.Entry<String, Long> entry : gradeDistribution.entrySet()) {
                report.append(String.format("  %s: %d\n", entry.getKey(), entry.getValue()));
            }
        }

        // Academic standing
        report.append("\nAcademic Standing: ");
        if (cumulativeGPA >= 3.5) {
            report.append("Summa Cum Laude (Highest Honors)\n");
        } else if (cumulativeGPA >= 3.25) {
            report.append("Magna Cum Laude (High Honors)\n");
        } else if (cumulativeGPA >= 3.0) {
            report.append("Cum Laude (Honors)\n");
        } else if (cumulativeGPA >= 2.0) {
            report.append("Good Standing\n");
        } else {
            report.append("Academic Probation\n");
        }

        return report.toString();
    }
}