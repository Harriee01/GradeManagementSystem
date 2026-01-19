package com.grademanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class StudentTest {
    private Student regularStudent;
    private Student honorsStudent;
    private ContactInfo contactInfo;

    @BeforeEach
    void setUp() {
        contactInfo = new ContactInfo("john.doe@university.edu", "+1234567890", "123 Main St");
        regularStudent = new RegularStudent("S001", "John Doe", contactInfo);
        honorsStudent = new HonorsStudent("H001", "Jane Smith", contactInfo);
    }

    @Test
    @DisplayName("Test student creation and getters")
    void testStudentCreation() {
        assertEquals("S001", regularStudent.getId());
        assertEquals("John Doe", regularStudent.getName());
        assertEquals(contactInfo, regularStudent.getContactInfo());
        assertTrue(regularStudent.isActive());
    }

    @Test
    @DisplayName("Test regular student passing grade")
    void testRegularStudentPassingGrade() {
        assertEquals(50.0, regularStudent.getPassingGrade());
    }

    @Test
    @DisplayName("Test honors student passing grade")
    void testHonorsStudentPassingGrade() {
        assertEquals(60.0, honorsStudent.getPassingGrade());
    }

    @Test
    @DisplayName("Test grade addition and average calculation")
    void testGradeCalculation() {
        // Create mock grades
        Grade grade1 = createMockGrade(85.0);
        Grade grade2 = createMockGrade(90.0);

        regularStudent.addGrade((com.grademanagement.model.Grade) grade1);
        regularStudent.addGrade((com.grademanagement.model.Grade) grade2);

        assertEquals(2, regularStudent.getGrades().size());
        assertEquals(87.5, regularStudent.calculateAverageGrade(), 0.01);
    }

    @Test
    @DisplayName("Test GPA calculation")
    void testGPACalculation() {
        Grade grade1 = createMockGrade(92.0); // A
        Grade grade2 = createMockGrade(88.0); // B+

        regularStudent.addGrade((com.grademanagement.model.Grade) grade1);
        regularStudent.addGrade((com.grademanagement.model.Grade) grade2);

        assertEquals(3.65, regularStudent.calculateGPA(), 0.01);
    }

    @Test
    @DisplayName("Test honors eligibility")
    void testHonorsEligibility() {
        Grade grade1 = createMockGrade(90.0);
        Grade grade2 = createMockGrade(95.0);

        honorsStudent.addGrade((com.grademanagement.model.Grade) grade1);
        honorsStudent.addGrade((com.grademanagement.model.Grade) grade2);

        assertTrue(honorsStudent.isEligibleForHonors(honorsStudent.calculateAverageGrade()));
        assertFalse(regularStudent.isEligibleForHonors(regularStudent.calculateAverageGrade()));
    }

    @Test
    @DisplayName("Test student equality")
    void testStudentEquality() {
        Student student1 = new RegularStudent("S001", "John Doe", contactInfo);
        Student student2 = new RegularStudent("S001", "John Doe", contactInfo);
        Student student3 = new RegularStudent("S002", "Jane Smith", contactInfo);

        assertEquals(student1, student2);
        assertNotEquals(student1, student3);
    }

    @Test
    @DisplayName("Test student validation - invalid ID")
    void testInvalidStudentId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RegularStudent("001", "John Doe", contactInfo); // Should start with letter
        });
    }

    @Test
    @DisplayName("Test student validation - invalid name")
    void testInvalidStudentName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RegularStudent("S001", "J", contactInfo); // Name too short
        });
    }

    private Grade createMockGrade(double score) {
        // Create a simple mock grade
        return new Grade() {
            @Override
            public double getScore() {
                return score;
            }

            @Override
            public double getGPA() {
                if (score >= 93) return 4.0;
                else if (score >= 90) return 3.7;
                else if (score >= 87) return 3.3;
                else if (score >= 83) return 3.0;
                else if (score >= 80) return 2.7;
                else if (score >= 77) return 2.3;
                else if (score >= 73) return 2.0;
                else if (score >= 70) return 1.7;
                else if (score >= 67) return 1.3;
                else if (score >= 65) return 1.0;
                else return 0.0;
            }
        };
    }

    // Simple Grade interface for testing
    interface Grade {
        double getScore();
        double getGPA();
    }
}
