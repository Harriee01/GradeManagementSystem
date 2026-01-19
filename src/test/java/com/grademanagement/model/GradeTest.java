package com.grademanagement.model;

import com.grademanagement.model.subjects.AbstractSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class GradeTest {
    private Student mockStudent;
    private AbstractSubject mockSubject;

    @BeforeEach
    void setUp() {
        mockStudent = mock(Student.class);
        when(mockStudent.getPassingGrade()).thenReturn(50.0);
        when(mockStudent.getId()).thenReturn("S001");

        mockSubject = mock(AbstractSubject.class);
        when(mockSubject.getName()).thenReturn("Mathematics");
    }

    @Test
    @DisplayName("Test grade creation with valid data")
    void testGradeCreation() throws Exception {
        Grade grade = new Grade(mockStudent, mockSubject, 85.5, "2024-01-18");

        assertEquals(mockStudent, grade.getStudent());
        assertEquals(mockSubject, grade.getSubject());
        assertEquals(85.5, grade.getScore(), 0.01);
        assertEquals("2024-01-18", grade.getDate());
        assertTrue(grade.isPassing());
    }

    @Test
    @DisplayName("Test grade creation with invalid score")
    void testInvalidGradeScore() {
        assertThrows(Exception.class, () -> {
            new Grade(mockStudent, mockSubject, -5.0, "2024-01-18");
        });

        assertThrows(Exception.class, () -> {
            new Grade(mockStudent, mockSubject, 105.0, "2024-01-18");
        });
    }

    @Test
    @DisplayName("Test grade creation with empty date")
    void testEmptyDate() {
        assertThrows(Exception.class, () -> {
            new Grade(mockStudent, mockSubject, 85.0, "");
        });
    }

    @Test
    @DisplayName("Test GPA calculation")
    void testGPACalculation() throws Exception {
        // Test various scores and expected GPAs
        testGradeAndGPA(95.0, 4.0);   // A
        testGradeAndGPA(92.0, 3.7);   // A-
        testGradeAndGPA(88.0, 3.3);   // B+
        testGradeAndGPA(85.0, 3.0);   // B
        testGradeAndGPA(82.0, 2.7);   // B-
        testGradeAndGPA(78.0, 2.3);   // C+
        testGradeAndGPA(75.0, 2.0);   // C
        testGradeAndGPA(72.0, 1.7);   // C-
        testGradeAndGPA(68.0, 1.3);   // D+
        testGradeAndGPA(65.0, 1.0);   // D
        testGradeAndGPA(50.0, 0.0);   // F
    }

    private void testGradeAndGPA(double score, double expectedGPA) throws Exception {
        Grade grade = new Grade(mockStudent, mockSubject, score, "2024-01-18");
        assertEquals(expectedGPA, grade.getGPA(), 0.01,
                String.format("Score %.1f should have GPA %.1f", score, expectedGPA));
    }

    @Test
    @DisplayName("Test passing status")
    void testPassingStatus() throws Exception {
        when(mockStudent.getPassingGrade()).thenReturn(60.0); // Honors student

        Grade passingGrade = new Grade(mockStudent, mockSubject, 75.0, "2024-01-18");
        Grade failingGrade = new Grade(mockStudent, mockSubject, 55.0, "2024-01-18");

        assertTrue(passingGrade.isPassing());
        assertFalse(failingGrade.isPassing());
    }

    @Test
    @DisplayName("Test grade equality")
    void testGradeEquality() throws Exception {
        Grade grade1 = new Grade(mockStudent, mockSubject, 85.0, "2024-01-18");
        Grade grade2 = new Grade(mockStudent, mockSubject, 85.0, "2024-01-18");
        Grade grade3 = new Grade(mockStudent, mockSubject, 90.0, "2024-01-19");

        assertEquals(grade1, grade2);
        assertNotEquals(grade1, grade3);
    }

    @Test
    @DisplayName("Test grade comparison")
    void testGradeComparison() throws Exception {
        Grade grade1 = new Grade(mockStudent, mockSubject, 85.0, "2024-01-18");
        Grade grade2 = new Grade(mockStudent, mockSubject, 90.0, "2024-01-18");
        Grade grade3 = new Grade(mockStudent, mockSubject, 85.0, "2024-01-19");

        assertTrue(grade1.compareTo(grade2) < 0); // 85 < 90
        assertTrue(grade2.compareTo(grade1) > 0); // 90 > 85
        assertEquals(0, grade1.compareTo(grade3)); // 85 == 85
    }

    @Test
    @DisplayName("Test grade string representation")
    void testGradeToString() throws Exception {
        Grade grade = new Grade(mockStudent, mockSubject, 85.5, "2024-01-18");
        String str = grade.toString();

        assertTrue(str.contains("Mathematics"));
        assertTrue(str.contains("85.5"));
        assertTrue(str.contains("2024-01-18"));
        assertTrue(str.contains("PASS") || str.contains("FAIL"));
    }

    @Test
    @DisplayName("Test grade with semester and notes")
    void testGradeWithSemesterAndNotes() throws Exception {
        Grade grade = new Grade(mockStudent, mockSubject, 85.0, "2024-01-18",
                "Fall 2024", "Excellent work!");

        assertEquals("Fall 2024", grade.getSemester());
        assertEquals("Excellent work!", grade.getNotes());
    }
}