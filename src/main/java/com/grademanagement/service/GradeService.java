package com.grademanagement.service;

import com.grademanagement.model.Grade;
import com.grademanagement.model.Student;
import com.grademanagement.model.subjects.AbstractSubject;
import com.grademanagement.exceptions.GradeException;
import com.grademanagement.repository.GradeRepository;
import java.util.*;

// Service for grade-related operations
public class GradeService {
    private final GradeRepository gradeRepository;
    private final ValidationService validationService;

    public GradeService(GradeRepository gradeRepository, ValidationService validationService) {
        this.gradeRepository = gradeRepository;
        this.validationService = validationService;
    }

    // Record a new grade with validation
    public String recordGrade(Student student, AbstractSubject subject,
                              double score, String date, String semester, String notes)
            throws GradeException {

        // Validate inputs
        validateGradeInputs(student, subject, score, date);

        // Create grade object
        Grade grade = new Grade(student, subject, score, date, semester, notes);

        // Add to repository
        return gradeRepository.addGrade(grade);
    }

    // Record grade with minimal parameters
    public String recordGrade(Student student, AbstractSubject subject,
                              double score, String date) throws GradeException {
        return recordGrade(student, subject, score, date, "", "");
    }

    // Validate grade inputs
    private void validateGradeInputs(Student student, AbstractSubject subject,
                                     double score, String date) throws GradeException {
        if (student == null) {
            throw new GradeException("Student cannot be null");
        }
        if (subject == null) {
            throw new GradeException("Subject cannot be null");
        }
        if (!validationService.isValidGrade(String.valueOf(score))) {
            throw new GradeException("Invalid grade score: " + score);
        }
        if (date == null || date.trim().isEmpty()) {
            throw new GradeException("Date cannot be empty");
        }
    }

    // Update an existing grade
    public boolean updateGrade(String gradeId, double newScore, String newDate,
                               String newSemester, String newNotes) throws GradeException {

        Grade existingGrade = gradeRepository.getGrade(gradeId);
        if (existingGrade == null) {
            throw new GradeException("Grade not found with ID: " + gradeId);
        }

        // Create updated grade
        Grade updatedGrade = new Grade(
                existingGrade.getStudent(),
                existingGrade.getSubject(),
                newScore,
                newDate,
                newSemester,
                newNotes
        );

        return gradeRepository.updateGrade(gradeId, updatedGrade);
    }

    // Delete a grade
    public boolean deleteGrade(String gradeId) {
        return gradeRepository.deleteGrade(gradeId);
    }

    // Get grade by ID
    public Grade getGrade(String gradeId) {
        return gradeRepository.getGrade(gradeId);
    }

    // Get all grades for a student
    public List<Grade> getStudentGrades(String studentId) {
        return gradeRepository.getGradesByStudentId(studentId);
    }

    // Get grades by subject
    public List<Grade> getSubjectGrades(String subjectName) {
        return gradeRepository.getGradesBySubject(subjectName);
    }

    // Get grades by date range
    public List<Grade> getGradesByDateRange(String fromDate, String toDate) {
        return gradeRepository.getGradesByDateRange(fromDate, toDate);
    }

    // Get grades by semester
    public List<Grade> getGradesBySemester(String semester) {
        return gradeRepository.getGradesBySemester(semester);
    }

    // Get latest grades
    public List<Grade> getLatestGrades(int count) {
        return gradeRepository.getLatestGrades(count);
    }

    // Calculate student statistics
    public Map<String, Object> calculateStudentStatistics(String studentId) {
        return gradeRepository.getStudentStatistics(studentId);
    }

    // Get overall statistics
    public Map<String, Object> getOverallStatistics() {
        return gradeRepository.getOverallStatistics();
    }

    // Search grades with multiple criteria
    public List<Grade> searchGrades(String studentId, String subjectName,
                                    String fromDate, String toDate,
                                    Double minScore, Double maxScore) {
        return gradeRepository.searchGrades(studentId, subjectName, fromDate,
                toDate, minScore, maxScore);
    }

    // Calculate class average for a subject
    public double calculateClassAverageForSubject(String subjectName) {
        List<Grade> grades = gradeRepository.getGradesBySubject(subjectName);
        if (grades.isEmpty()) {
            return 0.0;
        }

        double sum = grades.stream()
                .mapToDouble(Grade::getScore)
                .sum();

        return sum / grades.size();
    }

    // Calculate pass rate for a subject
    public double calculateSubjectPassRate(String subjectName) {
        List<Grade> grades = gradeRepository.getGradesBySubject(subjectName);
        if (grades.isEmpty()) {
            return 0.0;
        }

        long passCount = grades.stream()
                .filter(Grade::isPassing)
                .count();

        return (passCount * 100.0) / grades.size();
    }

    // Get grade distribution for a subject
    public Map<String, Integer> getSubjectGradeDistribution(String subjectName) {
        List<Grade> grades = gradeRepository.getGradesBySubject(subjectName);
        Map<String, Integer> distribution = new HashMap<>();

        // Initialize with all possible grades
        String[] letters = {"A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F"};
        for (String letter : letters) {
            distribution.put(letter, 0);
        }

        // Count actual grades
        for (Grade grade : grades) {
            String letter = grade.getLetterGrade().getLetter();
            distribution.put(letter, distribution.get(letter) + 1);
        }

        return distribution;
    }

    // Export grades to CSV format
    public String exportGradesToCSV(List<Grade> grades) {
        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("GradeID,StudentID,StudentName,Subject,Score,LetterGrade,GPA,Date,Semester,Notes,Passing\n");

        // Data
        for (Grade grade : grades) {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%.2f,\"%s\",%.2f,\"%s\",\"%s\",\"%s\",%s\n",
                    "GRADE_" + System.identityHashCode(grade), // Temporary ID
                    grade.getStudent().getId(),
                    grade.getStudent().getName(),
                    grade.getSubject().getName(),
                    grade.getScore(),
                    grade.getLetterGrade().getLetter(),
                    grade.getGPA(),
                    grade.getDate(),
                    grade.getSemester(),
                    grade.getNotes().replace("\"", "\"\""), // Escape quotes
                    grade.isPassing() ? "YES" : "NO"
            ));
        }

        return csv.toString();
    }

    // Get service statistics
    public Map<String, Object> getServiceStatistics() {
        Map<String, Object> stats = gradeRepository.getOverallStatistics();
        stats.put("totalGradesInRepository", gradeRepository.getGradeCount());
        return stats;
    }
}
