package com.grademanagement.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.grademanagement.exceptions.FileImportException;
import com.grademanagement.model.*;
import com.grademanagement.model.enums.FileFormat;
import com.grademanagement.model.subjects.AbstractSubject;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

// Enhanced file service with NIO.2 and multiple format support
public class FileService {
    private final Gson gson;
    private final ValidationService validationService;

    public FileService(ValidationService validationService) {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Date.class, new com.google.gson.TypeAdapter<Date>() {
                    @Override
                    public void write(com.google.gson.stream.JsonWriter out, Date value) throws IOException {
                        out.value(value.getTime());
                    }
                    @Override
                    public Date read(com.google.gson.stream.JsonReader in) throws IOException {
                        return new Date(in.nextLong());
                    }
                })
                .create();
        this.validationService = validationService;
    }

    // Export data in multiple formats using NIO.2
    public void exportData(List<Student> students, Path outputPath, FileFormat format) throws IOException {
        if (students == null || outputPath == null || format == null) {
            throw new IllegalArgumentException("Invalid export parameters");
        }

        // Create parent directories if they don't exist
        Files.createDirectories(outputPath.getParent());

        switch (format) {
            case CSV:
                exportToCSV(students, outputPath);
                break;
            case JSON:
                exportToJSON(students, outputPath);
                break;
            case BINARY:
                exportToBinary(students, outputPath);
                break;
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    // Stream-based CSV export for large datasets
    private void exportToCSV(List<Student> students, Path outputPath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            // Write CSV header
            writer.write("StudentID,Name,Email,Phone,Type,AverageGrade,GPA,PassRate,Active\n");

            // Use stream for efficient processing
            students.stream()
                    .map(this::studentToCSV)
                    .forEach(line -> {
                        try {
                            writer.write(line);
                            writer.newLine();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
    }

    private String studentToCSV(Student student) {
        return String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%.2f,%.1f,%s",
                student.getId(),
                student.getName().replace("\"", "\"\""), // Escape quotes
                student.getContactInfo().getEmail(),
                student.getContactInfo().getPhone(),
                student.getStudentType(),
                student.calculateAverageGrade(),
                student.calculateGPA(),
              //  student.getPassRate(),
                student.isActive() ? "Yes" : "No"
        );
    }

    // JSON export with pretty printing
    private void exportToJSON(List<Student> students, Path outputPath) throws IOException {
        String json = gson.toJson(students);
        Files.writeString(outputPath, json,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    // Binary export for performance
    private void exportToBinary(List<Student> students, Path outputPath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                Files.newOutputStream(outputPath,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            oos.writeObject(students);
        }
    }

    // Export grade report for a single student
    public void exportStudentReport(Student student, String filePath) throws IOException {
        Path path = Paths.get(filePath);

        // Create parent directories if they don't exist
        Files.createDirectories(path.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(path,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            writer.write("=== STUDENT GRADE REPORT ===\n");
            writer.write(String.format("Student ID: %s\n", student.getId()));
            writer.write(String.format("Student Name: %s\n", student.getName()));
            writer.write(String.format("Student Type: %s\n", student.getStudentType()));
            writer.write(String.format("Passing Grade Required: %.1f%%\n", student.getPassingGrade()));
            writer.write(String.format("Average Grade: %.2f%%\n", student.calculateAverageGrade()));
            writer.write(String.format("Weighted Average: %.2f%%\n", student.calculateWeightedAverage()));
            writer.write(String.format("Cumulative GPA: %.2f\n", student.calculateGPA()));
            writer.write(String.format("Weighted GPA: %.2f\n", student.calculateWeightedGPA()));

            writer.write("\n=== CONTACT INFORMATION ===\n");
            writer.write(student.getContactInfo().toString() + "\n");

            writer.write("\n=== GRADE HISTORY ===\n");
            List<Grade> grades = student.getGrades();
            if (grades.isEmpty()) {
                writer.write("No grades recorded.\n");
            } else {
                // Sort by date (newest first)
                grades.sort((g1, g2) -> g2.getDate().compareTo(g1.getDate()));

                for (Grade grade : grades) {
                    writer.write(grade.toString() + "\n");
                }

                writer.write("\n=== SUMMARY ===\n");
                writer.write(String.format("Total Grades: %d\n", grades.size()));

                long passCount = grades.stream().filter(Grade::isPassing).count();
                writer.write(String.format("Passing Grades: %d\n", passCount));
                writer.write(String.format("Pass Rate: %.1f%%\n", (passCount * 100.0) / grades.size()));

                // Subject breakdown
                writer.write("\n=== SUBJECT BREAKDOWN ===\n");
                Map<String, List<Grade>> gradesBySubject = grades.stream()
                        .collect(Collectors.groupingBy(grade -> grade.getSubject().getName()));

                for (Map.Entry<String, List<Grade>> entry : gradesBySubject.entrySet()) {
                    double subjectAvg = entry.getValue().stream()
                            .mapToDouble(Grade::getScore)
                            .average()
                            .orElse(0.0);
                    writer.write(String.format("  %s: %.2f%% (Count: %d)\n",
                            entry.getKey(), subjectAvg, entry.getValue().size()));
                }
            }

            // Honors eligibility
            if (student.isEligibleForHonors(student.calculateAverageGrade())) {
                writer.write("\n★ HONORS ELIGIBLE ★\n");
            }

            writer.write("\nReport Generated: " + new Date() + "\n");
        }
    }

    // Import grades from CSV file (bulk import)
    public List<Grade> importGradesFromCSV(String filePath, List<Student> students,
                                           Map<String, AbstractSubject> subjects)
            throws FileImportException {

        Path path = Paths.get(filePath);

        // Validate file exists
        if (!Files.exists(path)) {
            throw new FileImportException("File not found: " + filePath);
        }

        List<Grade> importedGrades = new ArrayList<>();
        int lineNumber = 0;
        int successCount = 0;
        int errorCount = 0;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;

            // Skip header if exists
            if (reader.ready()) {
                reader.readLine();
                lineNumber++;
            }

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                try {
                    Grade grade = parseGradeCSVLine(line, students, subjects);
                    if (grade != null) {
                        importedGrades.add(grade);
                        successCount++;
                    } else {
                        errorCount++;
                    }
                } catch (Exception e) {
                    System.err.println("Line " + lineNumber + ": Error - " + e.getMessage());
                    errorCount++;
                }
            }

            System.out.println(String.format("Import completed: %d successful, %d failed",
                    successCount, errorCount));

        } catch (IOException e) {
            throw new FileImportException("Error reading file: " + e.getMessage(), e);
        }

        return importedGrades;
    }

    private Grade parseGradeCSVLine(String line, List<Student> students,
                                    Map<String, AbstractSubject> subjects) throws Exception {

        // Custom CSV parsing that handles quoted fields
        List<String> parts = parseCSVLine(line);

        if (parts.size() < 6) { // Minimum: studentId, subject, score, date
            System.err.println("Invalid CSV line (not enough columns): " + line);
            return null;
        }

        String studentId = parts.get(0).trim();
        String subjectName = parts.get(1).trim();
        double score = Double.parseDouble(parts.get(2).trim());
        String date = parts.get(3).trim();
        String semester = parts.size() > 4 ? parts.get(4).trim() : "";
        String notes = parts.size() > 5 ? parts.get(5).trim() : "";

        // Find student
        Student student = findStudentById(students, studentId);
        if (student == null) {
            System.err.println("Student not found: " + studentId);
            return null;
        }

        // Find or create subject
        AbstractSubject subject = subjects.get(subjectName);
        if (subject == null) {
            System.err.println("Subject not found: " + subjectName);
            return null;
        }

        // Validate grade
        if (!validationService.isValidGrade(String.valueOf(score))) {
            System.err.println("Invalid grade score: " + score);
            return null;
        }

        // Create grade
        return new Grade(student, subject, score, date, semester, notes);
    }

    // Parse CSV line with quoted fields
    private List<String> parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // Handle escaped quotes
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        // Add last field
        result.add(current.toString().trim());

        return result;
    }

    // Import students from CSV
    public List<Student> importStudentsFromCSV(String filePath) throws FileImportException {
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new FileImportException("File not found: " + filePath);
        }

        List<Student> importedStudents = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;

            // Skip header
            if (reader.ready()) {
                reader.readLine();
                lineNumber++;
            }

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                try {
                    Student student = parseStudentCSVLine(line);
                    if (student != null) {
                        importedStudents.add(student);
                    }
                } catch (Exception e) {
                    System.err.println("Line " + lineNumber + ": Error - " + e.getMessage());
                }
            }

        } catch (IOException e) {
            throw new FileImportException("Error reading file: " + e.getMessage(), e);
        }

        return importedStudents;
    }

    private Student parseStudentCSVLine(String line) throws Exception {
        List<String> parts = parseCSVLine(line);

        if (parts.size() < 5) {
            System.err.println("Invalid student CSV line: " + line);
            return null;
        }

        String id = parts.get(0).trim();
        String name = parts.get(1).trim();
        String email = parts.get(2).trim();
        String phone = parts.get(3).trim();
        String type = parts.get(4).trim();
        String address = parts.size() > 5 ? parts.get(5).trim() : "";

        // Validate inputs
        if (!validationService.isValidStudentId(id)) {
            throw new IllegalArgumentException("Invalid student ID: " + id);
        }

        if (!validationService.isValidEmail(email, null)) { // Don't check domain
            throw new IllegalArgumentException("Invalid email: " + email);
        }

        // Normalize phone
        phone = validationService.normalizePhone(phone);

        // Create contact info
        ContactInfo contactInfo = new ContactInfo(email, phone, address);

        // Create student
        if ("Honors".equalsIgnoreCase(type) || "Honors Student".equalsIgnoreCase(type)) {
            return new HonorsStudent(id, name, contactInfo);
        } else {
            return new RegularStudent(id, name, contactInfo);
        }
    }

    // Import from JSON
    public List<Student> importFromJSON(String filePath) throws FileImportException {
        try {
            String json = Files.readString(Paths.get(filePath));

            // Parse JSON array of students
            Student[] studentsArray = gson.fromJson(json, Student[].class);

            if (studentsArray == null) {
                return new ArrayList<>();
            }

            return Arrays.asList(studentsArray);

        } catch (IOException e) {
            throw new FileImportException("Error reading JSON file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new FileImportException("Error parsing JSON: " + e.getMessage(), e);
        }
    }

    // Import from binary
    public List<Student> importFromBinary(String filePath) throws FileImportException {
        try (ObjectInputStream ois = new ObjectInputStream(
                Files.newInputStream(Paths.get(filePath)))) {

            Object obj = ois.readObject();
            if (obj instanceof List) {
                return (List<Student>) obj;
            } else {
                throw new FileImportException("Invalid binary format");
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new FileImportException("Error reading binary file: " + e.getMessage(), e);
        }
    }

    // File watcher for automatic import
    public void watchImportDirectory(Path directory, java.util.function.Consumer<Path> fileHandler)
            throws IOException {

        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            directory.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            System.out.println("Watching directory: " + directory.toAbsolutePath());

            while (true) {
                WatchKey key;
                try {
                    key = watchService.take();  // Block until event occurs
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();
                    Path child = directory.resolve(filename);

                    // Handle file based on extension
                    if (Files.isRegularFile(child)) {
                        String extension = getFileExtension(filename.toString());
                        if (Arrays.asList("csv", "json", "bin").contains(extension.toLowerCase())) {
                            System.out.println("Detected file: " + filename);
                            fileHandler.accept(child);
                        }
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        }
    }

    private Student findStudentById(List<Student> students, String id) {
        return students.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }

    // Calculate pass rate for student (used in export)
    private double calculatePassRate(Student student) {
        List<Grade> grades = student.getGrades();
        if (grades.isEmpty()) return 0.0;

        long passCount = grades.stream()
                .filter(Grade::isPassing)
                .count();

        return (passCount * 100.0) / grades.size();
    }

    // Export audit logs to CSV
    public void exportAuditLogs(List<AuditLog> logs, Path outputPath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            writer.write("ID,Timestamp,Operation,UserID,EntityType,EntityID,Details,Success\n");

            for (AuditLog log : logs) {
                writer.write(log.toCSV());
                writer.newLine();
            }
        }
    }

    // Backup entire system state
    public void backupSystemState(Map<String, Object> systemState, Path backupDir) throws IOException {
        String timestamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        Path backupFile = backupDir.resolve("backup_" + timestamp + ".json");

        String json = gson.toJson(systemState);
        Files.writeString(backupFile, json);

        System.out.println("Backup created: " + backupFile.toAbsolutePath());
    }

    // Restore system state from backup
    public Map<String, Object> restoreSystemState(Path backupFile) throws IOException {
        String json = Files.readString(backupFile);

        // Parse JSON back to Map
        return gson.fromJson(json, Map.class);
    }


}
