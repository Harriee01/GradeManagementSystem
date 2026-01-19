package com.grademanagement.service;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.*;

// Comprehensive validation service using regex patterns
public class ValidationService {

    // Pre-compiled regex patterns for performance
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?\\d{1,3}?[-.\\s]?\\(?\\d{1,4}\\)?[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,9}$"
    );

    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile(
            "^[A-Z][A-Z0-9]{2,}$"
    );

    private static final Pattern COURSE_CODE_PATTERN = Pattern.compile(
            "^[A-Z]{3,4}\\d{3,4}$"
    );

    private static final Pattern GRADE_PATTERN = Pattern.compile(
            "^([0-9]{1,2}|100)(\\.[0-9]{1,2})?$"
    );

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "^\\d{4}-\\d{2}-\\d{2}$"
    );

    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[A-Za-z]{2,}(\\s+[A-Za-z]{2,})+$"
    );

    // Validate email with optional domain restriction
    public boolean isValidEmail(String email, String allowedDomain) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        Matcher matcher = EMAIL_PATTERN.matcher(email.trim());
        if (!matcher.matches()) {
            return false;
        }

        // Check domain if specified
        if (allowedDomain != null && !allowedDomain.isEmpty()) {
            String emailDomain = extractDomain(email);
            if (emailDomain == null) {
                return false;
            }

            // Check exact match or subdomain
            return emailDomain.equals(allowedDomain.toLowerCase()) ||
                    emailDomain.endsWith("." + allowedDomain.toLowerCase());
        }

        return true;
    }

    // Extract domain from email
    private String extractDomain(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex == -1) {
            return null;
        }
        return email.substring(atIndex + 1).toLowerCase();
    }

    // Validate and normalize phone number
    public String normalizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }

        String cleaned = phone.trim();

        if (!PHONE_PATTERN.matcher(cleaned).matches()) {
            throw new IllegalArgumentException("Invalid phone number format: " + phone);
        }

        // Remove all non-digit characters except leading +
        StringBuilder normalized = new StringBuilder();
        boolean hasPlus = cleaned.startsWith("+");

        if (hasPlus) {
            normalized.append('+');
            cleaned = cleaned.substring(1);
        }

        // Add digits
        for (char c : cleaned.toCharArray()) {
            if (Character.isDigit(c)) {
                normalized.append(c);
            }
        }

        String digits = normalized.toString();

        // Ensure proper formatting
        if (hasPlus) {
            return digits; // Keep international format
        } else if (digits.length() == 10) {
            return "+1" + digits;  // Assume US number
        } else if (digits.length() == 11 && digits.startsWith("1")) {
            return "+" + digits; // US number with country code
        }

        return "+" + digits; // Default international format
    }

    // Format phone number for display
    public String formatPhoneForDisplay(String phone) {
        try {
            String normalized = normalizePhone(phone);

            if (normalized.startsWith("+1") && normalized.length() == 12) {
                // US format: (XXX) XXX-XXXX
                String digits = normalized.substring(2);
                return String.format("(%s) %s-%s",
                        digits.substring(0, 3),
                        digits.substring(3, 6),
                        digits.substring(6));
            } else if (normalized.length() > 4) {
                // International format: +XX XXX XXX XXXX
                StringBuilder formatted = new StringBuilder();
                formatted.append(normalized.substring(0, Math.min(3, normalized.length())));

                for (int i = 3; i < normalized.length(); i += 3) {
                    int end = Math.min(i + 3, normalized.length());
                    formatted.append(" ").append(normalized.substring(i, end));
                }

                return formatted.toString();
            }

            return normalized;
        } catch (IllegalArgumentException e) {
            return phone; // Return original if can't normalize
        }
    }

    // Validate student ID with pattern and optional checksum
    public boolean isValidStudentId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }

        String cleaned = id.trim().toUpperCase();

        if (!STUDENT_ID_PATTERN.matcher(cleaned).matches()) {
            return false;
        }

        // Optional: Add checksum validation
        return validateChecksum(cleaned);
    }

    private boolean validateChecksum(String id) {
        // Simple checksum algorithm for demonstration
        if (id.length() < 4) return true;

        char lastChar = id.charAt(id.length() - 1);
        if (Character.isDigit(lastChar)) {
            int sum = 0;
            for (int i = 0; i < id.length() - 1; i++) {
                char c = id.charAt(i);
                if (Character.isDigit(c)) {
                    sum += Character.getNumericValue(c);
                } else if (Character.isLetter(c)) {
                    sum += (Character.toUpperCase(c) - 'A' + 1);
                }
            }
            int checksum = sum % 10;
            return checksum == Character.getNumericValue(lastChar);
        }
        return true;
    }

    // Generate student ID with checksum
    public String generateStudentId(String prefix, int number) {
        if (prefix == null || prefix.trim().isEmpty()) {
            prefix = "S";
        }

        if (number < 1) {
            number = 1;
        }

        String baseId = prefix.toUpperCase() + String.format("%03d", number);

        // Calculate checksum
        int sum = 0;
        for (char c : baseId.toCharArray()) {
            if (Character.isDigit(c)) {
                sum += Character.getNumericValue(c);
            } else if (Character.isLetter(c)) {
                sum += (Character.toUpperCase(c) - 'A' + 1);
            }
        }

        int checksum = sum % 10;
        return baseId + checksum;
    }

    // Validate course code
    public boolean isValidCourseCode(String code) {
        if (code == null) return false;
        return COURSE_CODE_PATTERN.matcher(code.trim()).matches();
    }

    // Validate grade range and format
    public boolean isValidGrade(String gradeStr) {
        if (gradeStr == null || gradeStr.trim().isEmpty()) {
            return false;
        }

        String trimmed = gradeStr.trim();

        if (!GRADE_PATTERN.matcher(trimmed).matches()) {
            return false;
        }

        try {
            double grade = Double.parseDouble(trimmed);
            return grade >= 0 && grade <= 100;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Validate date format (YYYY-MM-DD)
    public boolean isValidDate(String date) {
        if (date == null) return false;
        return DATE_PATTERN.matcher(date.trim()).matches();
    }

    // Validate name format
    public boolean isValidName(String name) {
        if (name == null) return false;
        return NAME_PATTERN.matcher(name.trim()).matches();
    }

    // Pattern-based search in text
    public List<String> findPatternMatches(String text, String regexPattern) {
        List<String> matches = new ArrayList<>();

        if (text == null || regexPattern == null) {
            return matches;
        }

        try {
            Pattern pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);

            while (matcher.find()) {
                matches.add(matcher.group());
            }
        } catch (Exception e) {
            System.err.println("Invalid regex pattern: " + e.getMessage());
        }

        return matches;
    }

    // Extract emails from text
    public List<String> extractEmails(String text) {
        return findPatternMatches(text, "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
    }

    // Extract phone numbers from text
    public List<String> extractPhoneNumbers(String text) {
        return findPatternMatches(text, "\\+?\\d{1,3}?[-.\\s]?\\(?\\d{1,4}\\)?[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,9}");
    }

    // Extract student IDs from text
    public List<String> extractStudentIds(String text) {
        return findPatternMatches(text, "\\b[A-Z][A-Z0-9]{2,}\\b");
    }

    // Validate password strength
    public Map<String, Boolean> validatePasswordStrength(String password) {
        Map<String, Boolean> checks = new HashMap<>();

        if (password == null) {
            password = "";
        }

        // Minimum length
        checks.put("minLength", password.length() >= 8);

        // Contains uppercase
        checks.put("hasUppercase", Pattern.compile("[A-Z]").matcher(password).find());

        // Contains lowercase
        checks.put("hasLowercase", Pattern.compile("[a-z]").matcher(password).find());

        // Contains digit
        checks.put("hasDigit", Pattern.compile("\\d").matcher(password).find());

        // Contains special character
        checks.put("hasSpecial", Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]").matcher(password).find());

        // No whitespace
        checks.put("noWhitespace", !Pattern.compile("\\s").matcher(password).find());

        return checks;
    }

    // Calculate password strength score (0-100)
    public int calculatePasswordScore(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int score = 0;

        // Length score
        if (password.length() >= 8) score += 20;
        if (password.length() >= 12) score += 10;
        if (password.length() >= 16) score += 10;

        // Character variety score
        Map<String, Boolean> checks = validatePasswordStrength(password);

        if (checks.get("hasUppercase")) score += 15;
        if (checks.get("hasLowercase")) score += 15;
        if (checks.get("hasDigit")) score += 15;
        if (checks.get("hasSpecial")) score += 15;
        if (checks.get("noWhitespace")) score += 10;

        // Bonus for no dictionary words (simple check)
        String[] commonWords = {"password", "123456", "qwerty", "admin", "welcome"};
        boolean hasCommonWord = false;
        for (String word : commonWords) {
            if (password.toLowerCase().contains(word)) {
                hasCommonWord = true;
                break;
            }
        }
        if (!hasCommonWord) score += 10;

        return Math.min(score, 100);
    }

    // Validate semester format (e.g., Fall 2024, Spring 2025)
    public boolean isValidSemester(String semester) {
        if (semester == null || semester.trim().isEmpty()) {
            return false;
        }

        String trimmed = semester.trim();
        Pattern pattern = Pattern.compile("^(Spring|Summer|Fall|Winter)\\s+\\d{4}$", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(trimmed).matches();
    }

    // Normalize semester string
    public String normalizeSemester(String semester) {
        if (!isValidSemester(semester)) {
            throw new IllegalArgumentException("Invalid semester format: " + semester);
        }

        String[] parts = semester.trim().split("\\s+");
        String season = parts[0];
        String year = parts[1];

        // Capitalize season
        season = season.substring(0, 1).toUpperCase() + season.substring(1).toLowerCase();

        return season + " " + year;
    }

    // Validate GPA value (0.0 - 4.0)
    public boolean isValidGPA(double gpa) {
        return gpa >= 0.0 && gpa <= 4.0;
    }

    // Validate credit hours (1-6 typically)
    public boolean isValidCreditHours(int credits) {
        return credits >= 1 && credits <= 6;
    }

    // Bulk validation of student data
    public Map<String, List<String>> validateStudentData(Map<String, String> studentData) {
        Map<String, List<String>> errors = new HashMap<>();

        // Validate each field
        if (studentData.containsKey("id")) {
            String id = studentData.get("id");
            if (!isValidStudentId(id)) {
                errors.computeIfAbsent("id", k -> new ArrayList<>())
                        .add("Invalid student ID format");
            }
        }

        if (studentData.containsKey("name")) {
            String name = studentData.get("name");
            if (!isValidName(name)) {
                errors.computeIfAbsent("name", k -> new ArrayList<>())
                        .add("Invalid name format. Must contain at least 2 words with letters only.");
            }
        }

        if (studentData.containsKey("email")) {
            String email = studentData.get("email");
            if (!isValidEmail(email, null)) {
                errors.computeIfAbsent("email", k -> new ArrayList<>())
                        .add("Invalid email format");
            }
        }

        if (studentData.containsKey("phone")) {
            String phone = studentData.get("phone");
            try {
                normalizePhone(phone);
            } catch (IllegalArgumentException e) {
                errors.computeIfAbsent("phone", k -> new ArrayList<>())
                        .add("Invalid phone number: " + e.getMessage());
            }
        }

        return errors;
    }

    // Generate validation report
    public String generateValidationReport(Map<String, List<String>> errors) {
        if (errors.isEmpty()) {
            return "✓ All validations passed successfully.";
        }

        StringBuilder report = new StringBuilder();
        report.append("Validation Errors:\n");

        for (Map.Entry<String, List<String>> entry : errors.entrySet()) {
            report.append("  ").append(entry.getKey()).append(":\n");
            for (String error : entry.getValue()) {
                report.append("    - ").append(error).append("\n");
            }
        }

        return report.toString();
    }
}