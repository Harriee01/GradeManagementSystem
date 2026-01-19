package com.grademanagement.model;

import com.grademanagement.model.interfaces.Identifiable;
import com.grademanagement.model.interfaces.Cacheable;
import com.grademanagement.model.interfaces.StudentType;
import com.grademanagement.model.subjects.CoreSubject;
import com.grademanagement.model.subjects.ElectiveSubject;

import java.util.*;
import java.util.regex.Pattern;

// Abstract base class for all students
public abstract class Student implements Identifiable<String>, StudentType, Cacheable {
    private String id;
    private String name;
    private ContactInfo contactInfo;
    private List<Grade> grades;
    private long creationTime;
    private Date enrollmentDate;
    private boolean active;

    public Student(String id, String name, ContactInfo contactInfo) {
        validateId(id);
        validateName(name);

        this.id = id;
        this.name = name;
        this.contactInfo = contactInfo;
        this.grades = new ArrayList<>();
        this.creationTime = System.currentTimeMillis();
        this.enrollmentDate = new Date();
        this.active = true;
    }

    // Regex validation methods
    private void validateId(String id) {
        // Student ID pattern: Letter followed by 3+ digits (e.g., S001, CS2024001)
        Pattern idPattern = Pattern.compile("^[A-Z][A-Z0-9]{2,}$");
        if (id == null || !idPattern.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid student ID format: " + id);
        }
    }

    private void validateName(String name) {
        // Name pattern: At least 2 words, letters and spaces only
        Pattern namePattern = Pattern.compile("^[A-Za-z]{2,}(\\s+[A-Za-z]{2,})+$");
        if (name == null || !namePattern.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid name format: " + name);
        }
    }

    // Cacheable interface implementation
    @Override
    public String getCacheKey() {
        return "STUDENT_" + id;  // Unique cache key
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public boolean isExpired(long timeout) {
        return (System.currentTimeMillis() - creationTime) > timeout;
    }

    // Identifiable interface implementation
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        validateId(id);
        this.id = id;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        validateName(name);
        this.name = name;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public List<Grade> getGrades() {
        return new ArrayList<>(grades); // Return copy for encapsulation
    }

    public Date getEnrollmentDate() {
        return new Date(enrollmentDate.getTime()); // Return copy
    }

    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = new Date(enrollmentDate.getTime());
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Add a grade to student's record
    public void addGrade(Grade grade) {
        if (grade.getStudent() != this) {
            throw new IllegalArgumentException("Grade does not belong to this student");
        }
        grades.add(grade);
    }

    // Calculate average grade
    public double calculateAverageGrade() {
        if (grades.isEmpty()) {
            return 0.0;
        }

        double total = grades.stream()
                .mapToDouble(Grade::getScore)
                .sum();

        return total / grades.size();
    }

    // Calculate weighted average (by credit hours)
    public double calculateWeightedAverage() {
        if (grades.isEmpty()) {
            return 0.0;
        }

        double weightedSum = 0;
        int totalCredits = 0;

        for (Grade grade : grades) {
            int credits = 3; // Default
            if (grade.getSubject() instanceof CoreSubject) {
                credits = ((CoreSubject) grade.getSubject()).getCreditHours();
            } else if (grade.getSubject() instanceof ElectiveSubject) {
                credits = ((ElectiveSubject) grade.getSubject()).getCreditHours();
            }

            weightedSum += grade.getScore() * credits;
            totalCredits += credits;
        }

        return totalCredits > 0 ? weightedSum / totalCredits : 0.0;
    }

    // Calculate cumulative GPA
    public double calculateGPA() {
        if (grades.isEmpty()) {
            return 0.0;
        }

        double totalGPA = grades.stream()
                .mapToDouble(Grade::getGPA)
                .sum();

        return totalGPA / grades.size();
    }

    // Calculate weighted GPA
    public double calculateWeightedGPA() {
        if (grades.isEmpty()) {
            return 0.0;
        }

        double weightedSum = 0;
        int totalCredits = 0;

        for (Grade grade : grades) {
            int credits = 3; // Default
            if (grade.getSubject() instanceof CoreSubject) {
                credits = ((CoreSubject) grade.getSubject()).getCreditHours();
            } else if (grade.getSubject() instanceof ElectiveSubject) {
                credits = ((ElectiveSubject) grade.getSubject()).getCreditHours();
            }

            weightedSum += grade.calculateWeightedGPA();
            totalCredits += credits;
        }

        return totalCredits > 0 ? weightedSum / totalCredits : 0.0;
    }

    // Get grades by subject type
    public List<Grade> getGradesBySubjectType(String type) {
        return grades.stream()
                .filter(grade -> grade.getSubject().getSubjectType().equals(type))
                .collect(java.util.stream.Collectors.toList());
    }

    // Get grades by semester
    public List<Grade> getGradesBySemester(String semester) {
        return grades.stream()
                .filter(grade -> semester.equals(grade.getSemester()))
                .collect(java.util.stream.Collectors.toList());
    }

    // Get pass rate
//    public double getPassRate() {
//        if (grades.isEmpty()) {
//            return 0.0;
//        }
//
//        long passCount = grades.stream()
//                .filter(Grade::isPassing)
//                .count();
//
//        return (passCount * 100.0) / grades.size();
//    }

    // Abstract method - must be implemented by child classes
    public abstract String getStudentType();

    // Display student information
    @Override
    public String toString() {
        return String.format("ID: %s, Name: %s, Type: %s, Email: %s, Avg Grade: %.2f, GPA: %.2f, Active: %s",
                id, name, getStudentType(), contactInfo.getEmail(),
                calculateAverageGrade(), calculateGPA(), active ? "Yes" : "No");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Student student = (Student) obj;
        return id.equals(student.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
