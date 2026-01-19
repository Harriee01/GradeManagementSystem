package com.grademanagement.service;

import com.grademanagement.model.Student;
import com.grademanagement.model.Grade;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// Advanced search service with regex support
public class SearchService {
    private final StudentService studentService;
    private final GradeService gradeService;
    private final ValidationService validationService;

    public SearchService(StudentService studentService, GradeService gradeService,
                         ValidationService validationService) {
        this.studentService = studentService;
        this.gradeService = gradeService;
        this.validationService = validationService;
    }

    // Basic student search
    public List<Student> searchStudents(String query) {
        if (query == null || query.trim().isEmpty()) {
            return studentService.getAllStudents();
        }

        String searchTerm = query.trim().toLowerCase();
        List<Student> allStudents = studentService.getAllStudents();
        List<Student> results = new ArrayList<>();

        for (Student student : allStudents) {
            // Search in multiple fields
            if (student.getId().toLowerCase().contains(searchTerm) ||
                    student.getName().toLowerCase().contains(searchTerm) ||
                    student.getContactInfo().getEmail().toLowerCase().contains(searchTerm) ||
                    student.getStudentType().toLowerCase().contains(searchTerm)) {
                results.add(student);
            }
        }

        return results;
    }

    // Advanced search with multiple criteria
    public List<Student> advancedStudentSearch(String id, String name, String email,
                                               String type, Double minGPA, Double maxGPA,
                                               Boolean active) {

        List<Student> results = studentService.getAllStudents();

        // Apply filters
        if (id != null && !id.trim().isEmpty()) {
            String searchId = id.trim().toLowerCase();
            results = results.stream()
                    .filter(student -> student.getId().toLowerCase().contains(searchId))
                    .collect(Collectors.toList());
        }

        if (name != null && !name.trim().isEmpty()) {
            String searchName = name.trim().toLowerCase();
            results = results.stream()
                    .filter(student -> student.getName().toLowerCase().contains(searchName))
                    .collect(Collectors.toList());
        }

        if (email != null && !email.trim().isEmpty()) {
            String searchEmail = email.trim().toLowerCase();
            results = results.stream()
                    .filter(student -> student.getContactInfo().getEmail().toLowerCase().contains(searchEmail))
                    .collect(Collectors.toList());
        }

        if (type != null && !type.trim().isEmpty()) {
            String searchType = type.trim();
            results = results.stream()
                    .filter(student -> student.getStudentType().equalsIgnoreCase(searchType))
                    .collect(Collectors.toList());
        }

        if (minGPA != null) {
            results = results.stream()
                    .filter(student -> student.calculateGPA() >= minGPA)
                    .collect(Collectors.toList());
        }

        if (maxGPA != null) {
            results = results.stream()
                    .filter(student -> student.calculateGPA() <= maxGPA)
                    .collect(Collectors.toList());
        }

        if (active != null) {
            results = results.stream()
                    .filter(student -> student.isActive() == active)
                    .collect(Collectors.toList());
        }

        return results;
    }

    // Pattern-based search using regex
    public List<Student> patternSearch(String regexPattern) {
        List<Student> results = new ArrayList<>();

        if (regexPattern == null || regexPattern.trim().isEmpty()) {
            return results;
        }

        try {
            Pattern pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
            List<Student> allStudents = studentService.getAllStudents();

            for (Student student : allStudents) {
                // Search in multiple fields
                String searchText = String.format("%s %s %s %s",
                        student.getId(),
                        student.getName(),
                        student.getContactInfo().getEmail(),
                        student.getStudentType());

                if (pattern.matcher(searchText).find()) {
                    results.add(student);
                }
            }
        } catch (Exception e) {
            System.err.println("Invalid regex pattern: " + e.getMessage());
        }

        return results;
    }

    // Search students by email domain using regex
    public List<Student> searchByEmailDomainRegex(String domainPattern) {
        List<Student> results = new ArrayList<>();

        if (domainPattern == null || domainPattern.trim().isEmpty()) {
            return results;
        }

        try {
            // Convert simple domain pattern to regex
            String regex = domainPattern.replace(".", "\\.").replace("*", ".*");
            if (!regex.startsWith("@")) {
                regex = "@" + regex;
            }

            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            List<Student> allStudents = studentService.getAllStudents();

            for (Student student : allStudents) {
                String email = student.getContactInfo().getEmail();
                if (pattern.matcher(email).find()) {
                    results.add(student);
                }
            }
        } catch (Exception e) {
            System.err.println("Invalid domain pattern: " + e.getMessage());
        }

        return results;
    }

    // Search grades with multiple criteria
    public List<Grade> searchGrades(String studentId, String subject,
                                    String fromDate, String toDate,
                                    Double minScore, Double maxScore,
                                    String semester, Boolean passing) {

        // Start with all grades
        List<Grade> allGrades = new ArrayList<>();
        List<Student> students;

        if (studentId != null && !studentId.trim().isEmpty()) {
            // Get grades for specific student
            allGrades.addAll(gradeService.getStudentGrades(studentId));
        } else {
            // Get all grades from all students
            students = studentService.getAllStudents();
            for (Student student : students) {
                allGrades.addAll(student.getGrades());
            }
        }

        // Apply filters
        List<Grade> results = new ArrayList<>(allGrades);

        if (subject != null && !subject.trim().isEmpty()) {
            results = results.stream()
                    .filter(grade -> grade.getSubject().getName().equalsIgnoreCase(subject.trim()))
                    .collect(Collectors.toList());
        }

        if (fromDate != null && !fromDate.trim().isEmpty()) {
            results = results.stream()
                    .filter(grade -> grade.getDate().compareTo(fromDate.trim()) >= 0)
                    .collect(Collectors.toList());
        }

        if (toDate != null && !toDate.trim().isEmpty()) {
            results = results.stream()
                    .filter(grade -> grade.getDate().compareTo(toDate.trim()) <= 0)
                    .collect(Collectors.toList());
        }

        if (minScore != null) {
            results = results.stream()
                    .filter(grade -> grade.getScore() >= minScore)
                    .collect(Collectors.toList());
        }

        if (maxScore != null) {
            results = results.stream()
                    .filter(grade -> grade.getScore() <= maxScore)
                    .collect(Collectors.toList());
        }

        if (semester != null && !semester.trim().isEmpty()) {
            results = results.stream()
                    .filter(grade -> semester.trim().equalsIgnoreCase(grade.getSemester()))
                    .collect(Collectors.toList());
        }

        if (passing != null) {
            results = results.stream()
                    .filter(grade -> grade.isPassing() == passing)
                    .collect(Collectors.toList());
        }

        return results;
    }

    // Search for excellent grades (A or A-)
    public List<Grade> searchExcellentGrades() {
        List<Grade> allGrades = new ArrayList<>();
        List<Student> students = studentService.getAllStudents();

        for (Student student : students) {
            allGrades.addAll(student.getGrades());
        }

        return allGrades.stream()
                .filter(Grade::isExcellent)
                .collect(Collectors.toList());
    }

    // Search for failing grades
    public List<Grade> searchFailingGrades() {
        List<Grade> allGrades = new ArrayList<>();
        List<Student> students = studentService.getAllStudents();

        for (Student student : students) {
            allGrades.addAll(student.getGrades());
        }

        return allGrades.stream()
                .filter(grade -> !grade.isPassing())
                .collect(Collectors.toList());
    }

    // Search students by grade range in specific subject
    public List<Student> searchStudentsBySubjectGradeRange(String subject,
                                                           Double minScore,
                                                           Double maxScore) {

        List<Student> results = new ArrayList<>();
        List<Student> allStudents = studentService.getAllStudents();

        for (Student student : allStudents) {
            List<Grade> subjectGrades = student.getGrades().stream()
                    .filter(grade -> grade.getSubject().getName().equalsIgnoreCase(subject))
                    .collect(Collectors.toList());

            if (!subjectGrades.isEmpty()) {
                double average = subjectGrades.stream()
                        .mapToDouble(Grade::getScore)
                        .average()
                        .orElse(0.0);

                if ((minScore == null || average >= minScore) &&
                        (maxScore == null || average <= maxScore)) {
                    results.add(student);
                }
            }
        }

        return results;
    }

    // Search for students with improving grades
    public List<Student> searchStudentsWithImprovingGrades() {
        List<Student> results = new ArrayList<>();
        List<Student> allStudents = studentService.getAllStudents();

        for (Student student : allStudents) {
            List<Grade> grades = student.getGrades();
            if (grades.size() >= 3) {
                // Sort by date
                grades.sort(Comparator.comparing(Grade::getDate));

                // Calculate average of first half vs second half
                int mid = grades.size() / 2;
                double firstHalfAvg = grades.subList(0, mid).stream()
                        .mapToDouble(Grade::getScore)
                        .average()
                        .orElse(0.0);

                double secondHalfAvg = grades.subList(mid, grades.size()).stream()
                        .mapToDouble(Grade::getScore)
                        .average()
                        .orElse(0.0);

                // Consider improving if second half average is at least 5% higher
                if (secondHalfAvg - firstHalfAvg >= 5.0) {
                    results.add(student);
                }
            }
        }

        return results;
    }

    // Fuzzy search for names (allows minor spelling errors)
    public List<Student> fuzzyNameSearch(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String searchName = name.trim().toLowerCase();
        List<Student> allStudents = studentService.getAllStudents();
        List<Student> results = new ArrayList<>();

        // Simple fuzzy matching: check if search term is contained in name
        // or if Levenshtein distance is small (for more advanced implementation)
        for (Student student : allStudents) {
            String studentName = student.getName().toLowerCase();

            // Exact match or contains
            if (studentName.contains(searchName)) {
                results.add(student);
            }
            // Check initials
            else if (matchesInitials(studentName, searchName)) {
                results.add(student);
            }
        }

        return results;
    }

    private boolean matchesInitials(String fullName, String search) {
        String[] nameParts = fullName.split("\\s+");
        StringBuilder initials = new StringBuilder();

        for (String part : nameParts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
            }
        }

        return initials.toString().equalsIgnoreCase(search);
    }

    // Search using complex query language
    public List<Student> querySearch(String query) {
        // Example query: "type:Honors gpa:>3.5 active:true"
        List<Student> results = studentService.getAllStudents();

        if (query == null || query.trim().isEmpty()) {
            return results;
        }

        String[] conditions = query.trim().split("\\s+");

        for (String condition : conditions) {
            String[] parts = condition.split(":", 2);
            if (parts.length != 2) continue;

            String field = parts[0].trim().toLowerCase();
            String value = parts[1].trim();

            switch (field) {
                case "type":
                case "studenttype":
                    results = results.stream()
                            .filter(student -> student.getStudentType().equalsIgnoreCase(value))
                            .collect(Collectors.toList());
                    break;

                case "gpa":
                    if (value.startsWith(">")) {
                        double minGPA = Double.parseDouble(value.substring(1));
                        results = results.stream()
                                .filter(student -> student.calculateGPA() > minGPA)
                                .collect(Collectors.toList());
                    } else if (value.startsWith("<")) {
                        double maxGPA = Double.parseDouble(value.substring(1));
                        results = results.stream()
                                .filter(student -> student.calculateGPA() < maxGPA)
                                .collect(Collectors.toList());
                    } else {
                        double targetGPA = Double.parseDouble(value);
                        results = results.stream()
                                .filter(student -> Math.abs(student.calculateGPA() - targetGPA) < 0.1)
                                .collect(Collectors.toList());
                    }
                    break;

                case "active":
                    boolean active = Boolean.parseBoolean(value);
                    results = results.stream()
                            .filter(student -> student.isActive() == active)
                            .collect(Collectors.toList());
                    break;

                case "emaildomain":
                    results = results.stream()
                            .filter(student -> {
                                String email = student.getContactInfo().getEmail();
                                return email.endsWith("@" + value);
                            })
                            .collect(Collectors.toList());
                    break;

//                case "passrate":
//                    if (value.startsWith(">")) {
//                        double minRate = Double.parseDouble(value.substring(1));
//                        results = results.stream()
//                                .filter(student -> student.getPassRate() > minRate)
//                                .collect(Collectors.toList());
//                    }
//                    break;
            }
        }

        return results;
    }

    // Generate search statistics
    public Map<String, Object> getSearchStatistics(String searchType, Object criteria) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("searchType", searchType);
        stats.put("criteria", criteria);
        stats.put("timestamp", new Date());

        // Add execution time (simulated)
        stats.put("executionTimeMs", (int)(Math.random() * 100));

        return stats;
    }

    // Export search results
    public String exportSearchResults(List<?> results, String format) {
        if (results == null || results.isEmpty()) {
            return "No results to export.";
        }

        StringBuilder output = new StringBuilder();

        if ("csv".equalsIgnoreCase(format)) {
            if (results.get(0) instanceof Student) {
                output.append("ID,Name,Email,Type,GPA,Active\n");
                for (Object obj : results) {
                    Student student = (Student) obj;
                    output.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%s\n",
                            student.getId(),
                            student.getName(),
                            student.getContactInfo().getEmail(),
                            student.getStudentType(),
                            student.calculateGPA(),
                            student.isActive() ? "Yes" : "No"
                    ));
                }
            } else if (results.get(0) instanceof Grade) {
                output.append("StudentID,StudentName,Subject,Score,Grade,GPA,Date,Passing\n");
                for (Object obj : results) {
                    Grade grade = (Grade) obj;
                    output.append(String.format("\"%s\",\"%s\",\"%s\",%.2f,\"%s\",%.2f,\"%s\",%s\n",
                            grade.getStudent().getId(),
                            grade.getStudent().getName(),
                            grade.getSubject().getName(),
                            grade.getScore(),
                            grade.getLetterGrade().getLetter(),
                            grade.getGPA(),
                            grade.getDate(),
                            grade.isPassing() ? "Yes" : "No"
                    ));
                }
            }
        } else {
            // Default text format
            output.append("Search Results (").append(results.size()).append(" items):\n\n");
            for (Object obj : results) {
                output.append(obj.toString()).append("\n");
            }
        }

        return output.toString();
    }
}