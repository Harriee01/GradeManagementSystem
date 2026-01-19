package com.grademanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class ValidationServiceTest {
    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    @Test
    @DisplayName("Test valid email validation")
    void testValidEmail() {
        assertTrue(validationService.isValidEmail("john.doe@university.edu", null));
        assertTrue(validationService.isValidEmail("jane.smith@company.com", null));
        assertTrue(validationService.isValidEmail("test123@domain.org", null));
    }

    @Test
    @DisplayName("Test invalid email validation")
    void testInvalidEmail() {
        assertFalse(validationService.isValidEmail("invalid-email", null));
        assertFalse(validationService.isValidEmail("john@.com", null));
        assertFalse(validationService.isValidEmail("@domain.com", null));
        assertFalse(validationService.isValidEmail("", null));
        assertFalse(validationService.isValidEmail(null, null));
    }

    @Test
    @DisplayName("Test email validation with domain restriction")
    void testEmailWithDomainRestriction() {
        assertTrue(validationService.isValidEmail("john@university.edu", "university.edu"));
        assertTrue(validationService.isValidEmail("jane@cs.university.edu", "university.edu"));
        assertFalse(validationService.isValidEmail("john@gmail.com", "university.edu"));
    }

    @Test
    @DisplayName("Test phone number normalization")
    void testPhoneNormalization() {
        assertEquals("+11234567890", validationService.normalizePhone("123-456-7890"));
        assertEquals("+11234567890", validationService.normalizePhone("(123) 456-7890"));
        assertEquals("+11234567890", validationService.normalizePhone("1234567890"));
        assertEquals("+441234567890", validationService.normalizePhone("+44 1234 567890"));
    }

    @Test
    @DisplayName("Test invalid phone number")
    void testInvalidPhone() {
        assertThrows(IllegalArgumentException.class, () -> {
            validationService.normalizePhone("invalid");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            validationService.normalizePhone("");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            validationService.normalizePhone(null);
        });
    }

    @Test
    @DisplayName("Test valid student ID")
    void testValidStudentId() {
        assertTrue(validationService.isValidStudentId("S001"));
        assertTrue(validationService.isValidStudentId("CS2024001"));
        assertTrue(validationService.isValidStudentId("A123"));
    }

    @Test
    @DisplayName("Test invalid student ID")
    void testInvalidStudentId() {
        assertFalse(validationService.isValidStudentId("001")); // Must start with letter
        assertFalse(validationService.isValidStudentId("AB")); // Too short
        assertFalse(validationService.isValidStudentId("")); // Empty
        assertFalse(validationService.isValidStudentId(null)); // Null
    }

    @Test
    @DisplayName("Test student ID generation")
    void testGenerateStudentId() {
        String id1 = validationService.generateStudentId("S", 1);
        String id2 = validationService.generateStudentId("CS", 2024);

        assertTrue(id1.startsWith("S"));
        assertTrue(id1.matches("^S\\d{3}\\d$")); // S001 with checksum

        assertTrue(id2.startsWith("CS"));
        assertTrue(id2.matches("^CS\\d{4}\\d$")); // CS2024 with checksum
    }

    @Test
    @DisplayName("Test valid grade validation")
    void testValidGrade() {
        assertTrue(validationService.isValidGrade("85.5"));
        assertTrue(validationService.isValidGrade("100"));
        assertTrue(validationService.isValidGrade("0"));
        assertTrue(validationService.isValidGrade("67.89"));
    }

    @Test
    @DisplayName("Test invalid grade validation")
    void testInvalidGrade() {
        assertFalse(validationService.isValidGrade("101")); // Too high
        assertFalse(validationService.isValidGrade("-5")); // Negative
        assertFalse(validationService.isValidGrade("abc")); // Not a number
        assertFalse(validationService.isValidGrade("")); // Empty
        assertFalse(validationService.isValidGrade(null)); // Null
    }

    @Test
    @DisplayName("Test date validation")
    void testDateValidation() {
        assertTrue(validationService.isValidDate("2024-01-18"));
        assertFalse(validationService.isValidDate("18-01-2024")); // Wrong format
        assertFalse(validationService.isValidDate("2024/01/18")); // Wrong separator
        assertFalse(validationService.isValidDate("")); // Empty
        assertFalse(validationService.isValidDate(null)); // Null
    }

    @Test
    @DisplayName("Test name validation")
    void testNameValidation() {
        assertTrue(validationService.isValidName("John Doe"));
        assertTrue(validationService.isValidName("Jane Ann Smith"));
        assertFalse(validationService.isValidName("J")); // Too short
        assertFalse(validationService.isValidName("John123")); // Contains numbers
        assertFalse(validationService.isValidName("")); // Empty
        assertFalse(validationService.isValidName(null)); // Null
    }

    @Test
    @DisplayName("Test password strength validation")
    void testPasswordStrength() {
        Map<String, Boolean> checks = validationService.validatePasswordStrength("StrongPass123!");

        assertTrue(checks.get("minLength"));
        assertTrue(checks.get("hasUppercase"));
        assertTrue(checks.get("hasLowercase"));
        assertTrue(checks.get("hasDigit"));
        assertTrue(checks.get("hasSpecial"));
        assertTrue(checks.get("noWhitespace"));
    }

    @Test
    @DisplayName("Test password score calculation")
    void testPasswordScore() {
        int weakScore = validationService.calculatePasswordScore("weak");
        int mediumScore = validationService.calculatePasswordScore("MediumPass1");
        int strongScore = validationService.calculatePasswordScore("StrongPass123!");

        assertTrue(weakScore < 50);
        assertTrue(mediumScore >= 50 && mediumScore < 80);
        assertTrue(strongScore >= 80);
    }

    @Test
    @DisplayName("Test pattern extraction from text")
    void testPatternExtraction() {
        String text = "Contact us at john@university.edu or jane@company.com or call +1234567890";

        List<String> emails = validationService.extractEmails(text);
        List<String> phones = validationService.extractPhoneNumbers(text);
        List<String> ids = validationService.extractStudentIds("Students: S001, CS2024, EE123");

        assertEquals(2, emails.size());
        assertTrue(emails.contains("john@university.edu"));
        assertTrue(emails.contains("jane@company.com"));

        assertEquals(1, phones.size());
        assertTrue(phones.contains("+1234567890"));

        assertEquals(3, ids.size());
        assertTrue(ids.contains("S001"));
        assertTrue(ids.contains("CS2024"));
        assertTrue(ids.contains("EE123"));
    }

    @Test
    @DisplayName("Test semester validation")
    void testSemesterValidation() {
        assertTrue(validationService.isValidSemester("Fall 2024"));
        assertTrue(validationService.isValidSemester("Spring 2025"));
        assertTrue(validationService.isValidSemester("Summer 2024"));
        assertTrue(validationService.isValidSemester("Winter 2025"));

        assertFalse(validationService.isValidSemester("Fall2024")); // No space
        assertFalse(validationService.isValidSemester("Autumn 2024")); // Invalid season
        assertFalse(validationService.isValidSemester("Fall 24")); // Short year
    }

    @Test
    @DisplayName("Test semester normalization")
    void testSemesterNormalization() {
        assertEquals("Fall 2024", validationService.normalizeSemester("fall 2024"));
        assertEquals("Spring 2025", validationService.normalizeSemester("SPRING 2025"));

        assertThrows(IllegalArgumentException.class, () -> {
            validationService.normalizeSemester("Invalid 2024");
        });
    }

    @Test
    @DisplayName("Test bulk student data validation")
    void testBulkStudentValidation() {
        Map<String, String> studentData = new HashMap<>();
        studentData.put("id", "S001");
        studentData.put("name", "John Doe");
        studentData.put("email", "john@university.edu");
        studentData.put("phone", "1234567890");

        Map<String, List<String>> errors = validationService.validateStudentData(studentData);

        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Test bulk student data validation with errors")
    void testBulkStudentValidationWithErrors() {
        Map<String, String> studentData = new HashMap<>();
        studentData.put("id", "001"); // Invalid: should start with letter
        studentData.put("name", "J"); // Invalid: too short
        studentData.put("email", "invalid-email"); // Invalid format
        studentData.put("phone", "invalid"); // Invalid phone

        Map<String, List<String>> errors = validationService.validateStudentData(studentData);

        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey("id"));
        assertTrue(errors.containsKey("name"));
        assertTrue(errors.containsKey("email"));
        assertTrue(errors.containsKey("phone"));
    }
}