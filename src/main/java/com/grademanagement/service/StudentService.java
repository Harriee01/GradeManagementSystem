package com.grademanagement.service;

import com.grademanagement.model.*;
import com.grademanagement.exceptions.StudentException;
import com.grademanagement.repository.StudentRepository;
import java.util.*;
import java.util.regex.Pattern;

// Service for student operations - follows Single Responsibility Principle
public class StudentService {
    private final StudentRepository studentRepository;
    private final ValidationService validationService;

    public StudentService(StudentRepository studentRepository, ValidationService validationService) {
        this.studentRepository = studentRepository;
        this.validationService = validationService;
    }

    // Add a new student with comprehensive validation
    public void addStudent(String id, String name, ContactInfo contactInfo, String type)
            throws StudentException {

        // Validate inputs
        validateStudentInputs(id, name, contactInfo, type);

        // Check if student already exists
        if (studentRepository.exists(id)) {
            throw new StudentException("Student with ID " + id + " already exists");
        }

        // Create appropriate student type
        Student student;
        if ("Honors".equalsIgnoreCase(type) || "Honors Student".equalsIgnoreCase(type)) {
            student = new HonorsStudent(id, name, contactInfo);
        } else {
            student = new RegularStudent(id, name, contactInfo);
        }

        // Add to repository
        studentRepository.addStudent(student);
    }

    // Validate all student inputs
    private void validateStudentInputs(String id, String name, ContactInfo contactInfo, String type)
            throws StudentException {

        if (!validationService.isValidStudentId(id)) {
            throw new StudentException("Invalid student ID format: " + id);
        }

        if (name == null || name.trim().isEmpty()) {
            throw new StudentException("Student name cannot be empty");
        }

        // Name validation pattern
        Pattern namePattern = Pattern.compile("^[A-Za-z]{2,}(\\s+[A-Za-z]{2,})+$");
        if (!namePattern.matcher(name).matches()) {
            throw new StudentException("Invalid name format. Must contain at least 2 words with letters only.");
        }

        if (contactInfo == null) {
            throw new StudentException("Contact information cannot be null");
        }

        // Validate email
        if (!validationService.isValidEmail(contactInfo.getEmail(), "university.edu")) {
            throw new StudentException("Invalid email. Must be valid format and from @university.edu domain.");
        }

        // Validate phone
        try {
            validationService.normalizePhone(contactInfo.getPhone());
        } catch (IllegalArgumentException e) {
            throw new StudentException("Invalid phone number: " + e.getMessage());
        }

        if (type == null || (!type.equalsIgnoreCase("Regular") && !type.equalsIgnoreCase("Honors"))) {
            throw new StudentException("Student type must be either 'Regular' or 'Honors'");
        }
    }

    // Get student by ID
    public Student getStudent(String id) throws StudentException {
        Student student = studentRepository.findById(id);
        if (student == null) {
            throw new StudentException("Student not found with ID: " + id);
        }
        return student;
    }

    // Search students by name (partial matching, case-insensitive)
    public List<Student> searchByName(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return studentRepository.findByName(searchTerm);
    }

    // Search student by exact ID
    public Student searchById(String id) throws StudentException {
        Student student = studentRepository.findById(id);
        if (student == null) {
            throw new StudentException("Student not found with ID: " + id);
        }
        return student;
    }

    // Search students by ID (partial matching)
    public List<Student> searchByPartialId(String partialId) {
        if (partialId == null || partialId.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Student> allStudents = studentRepository.getAllStudents();
        List<Student> results = new ArrayList<>();

        for (Student student : allStudents) {
            if (student.getId().toLowerCase().contains(partialId.toLowerCase())) {
                results.add(student);
            }
        }

        return results;
    }

    // Search by email domain
    public List<Student> searchByEmailDomain(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return studentRepository.findByEmailDomain(domain);
    }

    // Search by student type
    public List<Student> searchByType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return studentRepository.findByType(type);
    }

    // Advanced search with multiple criteria
    public List<Student> advancedSearch(String name, String emailDomain, String type) {
        return studentRepository.search(name, emailDomain, type);
    }

    // Update student information
    public boolean updateStudent(String id, String newName, ContactInfo newContactInfo,
                                 String newType) throws StudentException {

        Student existingStudent = getStudent(id);
        if (existingStudent == null) {
            return false;
        }

        // Validate new inputs
        validateStudentInputs(id, newName, newContactInfo, newType);

        // Create updated student
        Student updatedStudent;
        if ("Honors".equalsIgnoreCase(newType)) {
            updatedStudent = new HonorsStudent(id, newName, newContactInfo);
        } else {
            updatedStudent = new RegularStudent(id, newName, newContactInfo);
        }

        // Copy existing grades to updated student
        List<Grade> existingGrades = existingStudent.getGrades();
        for (Grade grade : existingGrades) {
            updatedStudent.addGrade(grade);
        }

        // Update in repository
        return studentRepository.updateStudent(id, updatedStudent);
    }

    // Delete student
    public boolean deleteStudent(String id) {
        return studentRepository.deleteStudent(id);
    }

    // Get all students
    public List<Student> getAllStudents() {
        return studentRepository.getAllStudents();
    }

    // Get students sorted by ID
    public List<Student> getAllStudentsSorted() {
        return studentRepository.getAllStudentsSorted();
    }

    // Get honors students
    public List<HonorsStudent> getHonorsStudents() {
        return studentRepository.getHonorsStudents();
    }

    // Get regular students
    public List<RegularStudent> getRegularStudents() {
        return studentRepository.getRegularStudents();
    }

    // Get active students count
    public long getActiveStudentCount() {
        return studentRepository.getActiveStudentCount();
    }

    // Get students with GPA above threshold
    public List<Student> getStudentsWithGpaAbove(double threshold) {
        return studentRepository.getStudentsWithGpaAbove(threshold);
    }

    // Get students with average grade above threshold
    public List<Student> getStudentsWithAverageAbove(double threshold) {
        return studentRepository.getStudentsWithAverageAbove(threshold);
    }

    // Calculate class statistics
    public Map<String, Object> calculateClassStatistics() {
        Map<String, Object> stats = studentRepository.getRepositoryMetrics();

        // Add additional calculated statistics
        List<Student> allStudents = getAllStudents();
        if (!allStudents.isEmpty()) {
            double totalAverage = allStudents.stream()
                    .mapToDouble(Student::calculateAverageGrade)
                    .average()
                    .orElse(0.0);
            stats.put("classAverageGrade", totalAverage);

            double totalGPA = allStudents.stream()
                    .mapToDouble(Student::calculateGPA)
                    .average()
                    .orElse(0.0);
            stats.put("classAverageGPA", totalGPA);

            // Count students eligible for honors
            long honorsEligible = allStudents.stream()
                    .filter(student -> student.isEligibleForHonors(student.calculateAverageGrade()))
                    .count();
            stats.put("honorsEligibleCount", honorsEligible);
        }

        return stats;
    }

    // Generate student report
    public String generateStudentReport(String studentId) throws StudentException {
        Student student = getStudent(studentId);
        if (student == null) {
            throw new StudentException("Student not found");
        }

        StringBuilder report = new StringBuilder();
        report.append("=== STUDENT REPORT ===\n");
        report.append(String.format("Student ID: %s\n", student.getId()));
        report.append(String.format("Name: %s\n", student.getName()));
        report.append(String.format("Type: %s\n", student.getStudentType()));
        report.append(String.format("Contact: %s\n", student.getContactInfo()));
        report.append(String.format("Enrollment Date: %s\n", student.getEnrollmentDate()));
        report.append(String.format("Status: %s\n", student.isActive() ? "Active" : "Inactive"));
        report.append(String.format("Passing Grade Required: %.1f%%\n", student.getPassingGrade()));

        // Academic statistics
//        report.append("\n=== ACADEMIC STATISTICS ===\n");
//        report.append(String.format("Average Grade: %.2f%%\n", student.calculateAverageGrade()));
//        report.append(String.format("Weighted Average: %.2f%%\n", student.calculateWeightedAverage()));
//        report.append(String.format("Cumulative GPA: %.2f\n", student.calculateGPA()));
//        report.append(String.format("Weighted GPA: %.2f\n", student.calculateWeightedGPA()));
//        report.append(String.format("Pass Rate: %.1f%%\n", student.getPassRate()));
//
//        // Honors eligibility
//        if (student instanceof HonorsStudent) {
//            HonorsStudent honorsStudent = (HonorsStudent) student;
//            report.append(String.format("Honors Program: %s\n", honorsStudent.getHonorsProgram()));
//            report.append(String.format("Honors Eligible: %s\n",
//                    student.isEligibleForHonors(student.calculateAverageGrade()) ? "YES" : "NO"));
//            report.append(String.format("Graduation Honors Eligible: %s\n",
//                    honorsStudent.isEligibleForGraduationHonors() ? "YES" : "NO"));
//        }

        // Grade history
        List<Grade> grades = student.getGrades();
        if (!grades.isEmpty()) {
            report.append("\n=== GRADE HISTORY ===\n");
            for (Grade grade : grades) {
                report.append("  - ").append(grade).append("\n");
            }
        } else {
            report.append("\nNo grades recorded yet.\n");
        }

        return report.toString();
    }

    // Validate student data (public version for external use)
    public void validateStudent(String id, String name) throws StudentException {
        if (id == null || id.trim().isEmpty()) {
            throw new StudentException("Student ID cannot be empty");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new StudentException("Student name cannot be empty");
        }

        // ID format validation (e.g., S001)
        if (!id.matches("[A-Za-z][0-9]{3,}")) {
            throw new StudentException("Student ID must start with a letter followed by numbers");
        }

        // Name validation (no numbers, at least 2 characters)
        if (!name.matches("[A-Za-z\\s]{2,}")) {
            throw new StudentException("Student name must contain only letters and spaces (min 2 chars)");
        }
    }

    // Get service statistics
    public Map<String, Object> getServiceStatistics() {
        return studentRepository.getRepositoryMetrics();
    }
}
