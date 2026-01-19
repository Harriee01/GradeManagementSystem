package com.grademanagement.service;

import com.grademanagement.model.*;
import com.grademanagement.exceptions.StudentException;
import com.grademanagement.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {
    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ValidationService validationService;

    private StudentService studentService;
    private ContactInfo contactInfo;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(studentRepository, validationService);
        contactInfo = new ContactInfo("john.doe@university.edu", "+1234567890", "123 Main St");
    }

    @Test
    @DisplayName("Test adding student successfully")
    void testAddStudentSuccess() throws StudentException {
        when(validationService.isValidStudentId("S001")).thenReturn(true);
        when(validationService.isValidEmail(anyString(), anyString())).thenReturn(true);
        when(studentRepository.exists("S001")).thenReturn(false);

        studentService.addStudent("S001", "John Doe", contactInfo, "Regular");

        verify(studentRepository).addStudent(any(Student.class));
    }

    @Test
    @DisplayName("Test adding student with existing ID")
    void testAddStudentDuplicateId() throws StudentException {
        when(validationService.isValidStudentId("S001")).thenReturn(true);
        when(validationService.isValidEmail(anyString(), anyString())).thenReturn(true);
        when(studentRepository.exists("S001")).thenReturn(true);

        assertThrows(StudentException.class, () -> {
            studentService.addStudent("S001", "John Doe", contactInfo, "Regular");
        });
    }

    @Test
    @DisplayName("Test getting student by ID")
    void testGetStudentById() throws StudentException {
        Student mockStudent = new RegularStudent("S001", "John Doe", contactInfo);
        when(studentRepository.findById("S001")).thenReturn(mockStudent);

        Student result = studentService.getStudent("S001");

        assertEquals(mockStudent, result);
    }

    @Test
    @DisplayName("Test getting non-existent student")
    void testGetNonExistentStudent() {
        when(studentRepository.findById("S999")).thenReturn(null);

        assertThrows(StudentException.class, () -> {
            studentService.getStudent("S999");
        });
    }

    @Test
    @DisplayName("Test searching students by name")
    void testSearchByName() {
        List<Student> mockStudents = Arrays.asList(
                new RegularStudent("S001", "John Doe", contactInfo),
                new RegularStudent("S002", "Jane Doe", contactInfo)
        );
        when(studentRepository.findByName("Doe")).thenReturn(mockStudents);

        List<Student> results = studentService.searchByName("Doe");

        assertEquals(2, results.size());
        verify(studentRepository).findByName("Doe");
    }

    @Test
    @DisplayName("Test searching by partial ID")
    void testSearchByPartialId() {
        List<Student> allStudents = Arrays.asList(
                new RegularStudent("CS001", "John Doe", contactInfo),
                new RegularStudent("CS002", "Jane Smith", contactInfo),
                new RegularStudent("EE001", "Bob Johnson", contactInfo)
        );
        when(studentRepository.getAllStudents()).thenReturn(allStudents);

        List<Student> results = studentService.searchByPartialId("CS");

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(s -> s.getId().startsWith("CS")));
    }

    @Test
    @DisplayName("Test advanced search")
    void testAdvancedSearch() {
        List<Student> mockResults = Arrays.asList(
                new RegularStudent("S001", "John Doe", contactInfo)
        );
        when(studentRepository.search("Doe", "university.edu", "Regular"))
                .thenReturn(mockResults);

        List<Student> results = studentService.advancedSearch("Doe", "university.edu", "Regular");

        assertEquals(1, results.size());
        verify(studentRepository).search("Doe", "university.edu", "Regular");
    }

    @Test
    @DisplayName("Test deleting student")
    void testDeleteStudent() {
        when(studentRepository.deleteStudent("S001")).thenReturn(true);

        boolean result = studentService.deleteStudent("S001");

        assertTrue(result);
        verify(studentRepository).deleteStudent("S001");
    }

    @Test
    @DisplayName("Test class statistics calculation")
    void testCalculateClassStatistics() {
        List<Student> mockStudents = Arrays.asList(
                new RegularStudent("S001", "John Doe", contactInfo),
                new HonorsStudent("H001", "Jane Smith", contactInfo)
        );
        when(studentRepository.getAllStudents()).thenReturn(mockStudents);

        // Mock repository metrics
        Map<String, Object> mockMetrics = new HashMap<>();
        mockMetrics.put("totalStudents", 2);
        mockMetrics.put("totalGrades", 10);
        when(studentRepository.getRepositoryMetrics()).thenReturn(mockMetrics);

        Map<String, Object> stats = studentService.calculateClassStatistics();

        assertNotNull(stats);
        assertEquals(2, stats.get("totalStudents"));
        assertTrue(stats.containsKey("classAverageGrade"));
        assertTrue(stats.containsKey("classAverageGPA"));
    }

    @Test
    @DisplayName("Test student validation method")
    void testValidateStudent() {
        // Test valid input
        assertDoesNotThrow(() -> {
            studentService.validateStudent("S001", "John Doe");
        });

        // Test invalid ID
        assertThrows(StudentException.class, () -> {
            studentService.validateStudent("", "John Doe");
        });

        // Test invalid name
        assertThrows(StudentException.class, () -> {
            studentService.validateStudent("S001", "");
        });
    }

    @Test
    @DisplayName("Test student report generation")
    void testGenerateStudentReport() throws StudentException {
        Student mockStudent = new RegularStudent("S001", "John Doe", contactInfo);
        when(studentRepository.findById("S001")).thenReturn(mockStudent);

        String report = studentService.generateStudentReport("S001");

        assertNotNull(report);
        assertTrue(report.contains("John Doe"));
        assertTrue(report.contains("S001"));
        assertTrue(report.contains("Regular Student"));
    }

    @Test
    @DisplayName("Test getting students with GPA above threshold")
    void testGetStudentsWithGpaAbove() {
        List<Student> mockStudents = Arrays.asList(
                new RegularStudent("S001", "John Doe", contactInfo),
                new RegularStudent("S002", "Jane Smith", contactInfo)
        );
        when(studentRepository.getStudentsWithGpaAbove(3.5)).thenReturn(mockStudents);

        List<Student> results = studentService.getStudentsWithGpaAbove(3.5);

        assertEquals(2, results.size());
        verify(studentRepository).getStudentsWithGpaAbove(3.5);
    }
}