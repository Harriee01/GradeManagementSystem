package com.grademanagement.model;

import com.grademanagement.model.enums.GradeLetter;
import com.grademanagement.model.subjects.AbstractSubject;
import com.grademanagement.exceptions.GradeException;
import com.grademanagement.model.subjects.CoreSubject;
import com.grademanagement.model.subjects.ElectiveSubject;

// Enhanced Grade class with GPA calculation and validation
public class Grade implements Comparable<Grade> {
    private Student student;           // Student who received the grade
    private AbstractSubject subject;   // Subject for the grade
    private double score;              // Grade score (0-100)
    private double gpa;                // GPA value (0.0-4.0)
    private GradeLetter letterGrade;   // Letter grade
    private String date;               // Date grade was recorded
    private String semester;           // Semester (e.g., "Fall 2024")
    private String notes;              // Additional notes

    public Grade(Student student, AbstractSubject subject, double score, String date)
            throws GradeException {
        this(student, subject, score, date, "", "");
    }

    public Grade(Student student, AbstractSubject subject, double score,
                 String date, String semester, String notes) throws GradeException {
        // Validate input parameters
        if (student == null) {
            throw new GradeException("Student cannot be null");
        }
        if (subject == null) {
            throw new GradeException("Subject cannot be null");
        }
        if (score < 0 || score > 100) {
            throw new GradeException("Grade must be between 0 and 100");
        }
        if (date == null || date.trim().isEmpty()) {
            throw new GradeException("Date cannot be empty");
        }

        this.student = student;
        this.subject = subject;
        this.score = score;
        this.date = date;
        this.semester = semester;
        this.notes = notes;
        this.gpa = calculateGPA();      // Calculate GPA on creation
        this.letterGrade = GradeLetter.fromPercentage(score); // Get letter grade
    }




    // Calculate GPA from percentage score (4.0 scale)
    private double calculateGPA() {
        return letterGrade.getGpaValue();
    }

    // Calculate weighted GPA (considering subject credits)
    public double calculateWeightedGPA() {
        double baseGPA = getGPA();
        int credits = 3; // Default

        if (subject instanceof CoreSubject) {
            credits = ((CoreSubject) subject).getCreditHours();
        } else if (subject instanceof ElectiveSubject) {
            credits = ((ElectiveSubject) subject).getCreditHours();
        }

        return baseGPA * credits;
    }

    // Getters
    public Student getStudent() {
        return student;
    }

    public AbstractSubject getSubject() {
        return subject;
    }

    public double getScore() {
        return score;
    }

    public double getGPA() {
        return gpa;
    }

    public GradeLetter getLetterGrade() {
        return letterGrade;
    }

    public String getDate() {
        return date;
    }

    public String getSemester() {
        return semester;
    }

    public String getNotes() {
        return notes;
    }

    // Setters with validation
    public void setScore(double score) throws GradeException {
        if (score < 0 || score > 100) {
            throw new GradeException("Grade must be between 0 and 100");
        }
        this.score = score;
        this.gpa = calculateGPA();  // Recalculate GPA when score changes
        this.letterGrade = GradeLetter.fromPercentage(score);
    }

    public void setDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("Date cannot be empty");
        }
        this.date = date;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Check if grade is passing based on student's passing requirement
//   public boolean isPassing() {
//       return score >= student.getPassingGrade();
//    }

    // Check if grade is excellent (A or A-)
    public boolean isExcellent() {
        return letterGrade == GradeLetter.A || letterGrade == GradeLetter.A_MINUS;
    }

    // Compare grades by score (for sorting)
    @Override
    public int compareTo(Grade other) {
        return Double.compare(this.score, other.score);
    }

    // Display grade information
    @Override
    public String toString() {
        return String.format("Subject: %s, Score: %.2f, Grade: %s (%.2f GPA), Date: %s, Status: %s",
                subject.getName(), score, letterGrade.getLetter(), gpa, date,
                isPassing() ? "PASS" : "FAIL");
    }

    public boolean isPassing() {
//        double average = calculateAverageGrade();
//        double passingGrade = getPassingGrade(); // Implement this in subclasses or define default

        return true;
    }
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) return true;
//        if (obj == null || getClass() != obj.getClass()) return false;
//        Grade grade = (Grade) obj;
//        return student.equals(grade.student) &&
//                subject.equals(grade.subject) &&
//                date.equals(grade.date);
//    }
//
//    @Override
//    public int hashCode() {
//        int result = student.hashCode();
//        result = 31 * result + subject.hashCode();
//        result = 31 * result + date.hashCode();
//        return result;
//    }

