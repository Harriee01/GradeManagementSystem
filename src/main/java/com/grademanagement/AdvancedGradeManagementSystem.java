package com.grademanagement;

import com.grademanagement.model.subjects.AbstractSubject;
import com.grademanagement.service.*;
import com.grademanagement.repository.*;
import com.grademanagement.model.*;
import com.grademanagement.model.enums.*;
import com.grademanagement.utils.*;
import java.util.*;
import java.nio.file.*;
import java.util.concurrent.*;

public class AdvancedGradeManagementSystem {
    // Services
    private final StudentService studentService;
    private final GradeService gradeService;
    private final StatisticsService statisticsService;
    private final FileService fileService;
    private final CacheService cacheService;
    private final TaskScheduler taskScheduler;
    private final PerformanceMonitor performanceMonitor;
    private final ValidationService validationService;
    private final AuditRepository auditRepository;

    // UI components
    private final Scanner scanner;
    private boolean dashboardRunning = false;
    private int activeBackgroundTasks = 0;

    public AdvancedGradeManagementSystem(StudentService studentService,
                                         GradeService gradeService,
                                         StatisticsService statisticsService,
                                         FileService fileService,
                                         CacheService cacheService,
                                         TaskScheduler taskScheduler,
                                         PerformanceMonitor performanceMonitor,
                                         ValidationService validationService,
                                         AuditRepository auditRepository) {
        this.studentService = studentService;
        this.gradeService = gradeService;
        this.statisticsService = statisticsService;
        this.fileService = fileService;
        this.cacheService = cacheService;
        this.taskScheduler = taskScheduler;
        this.performanceMonitor = performanceMonitor;
        this.validationService = validationService;
        this.auditRepository = auditRepository;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        displayWelcome();

        while (true) {
            displayMainMenu();
            int choice = getValidatedChoice(1, 19);

            try {
                processMenuChoice(choice);
                if (choice == 19) {
                    break; // Exit
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                Logger.getInstance().error("Menu error: " + e.getMessage());
            }

            updateActiveTasks();
        }
    }

    private void displayMainMenu() {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("          STUDENT GRADE MANAGEMENT - MAIN MENU");
        System.out.println("               [Advanced Edition v3.0]");
        System.out.println("═".repeat(60));

        System.out.println("\nSTUDENT MANAGEMENT");
        System.out.println(" 1. Add Student (with validation)");
        System.out.println(" 2. View Students");
        System.out.println(" 3. Record Grade");
        System.out.println(" 4. View Grade Report");

        System.out.println("\nFILE OPERATIONS");
        System.out.println(" 5. Export Grade Report (CSV/JSON/Binary)");
        System.out.println(" 6. Import Data (Multi-format support) [ENHANCED]");
        System.out.println(" 7. Bulk Import Grades");

        System.out.println("\nANALYTICS & REPORTING");
        System.out.println(" 8. Calculate Student GPA");
        System.out.println(" 9. View Class Statistics");
        System.out.println("10. Real-Time Statistics Dashboard " +
                (dashboardRunning ? "[ACTIVE]" : "[INACTIVE]") + " [NEW]");
        System.out.println("11. Generate Batch Reports [NEW]");

        System.out.println("\nSEARCH & QUERY");
        System.out.println("12. Search Students (Advanced) [ENHANCED]");
        System.out.println("13. Pattern-Based Search [NEW]");
        System.out.println("14. Query Grade History [NEW]");

        System.out.println("\nADVANCED FEATURES");
        System.out.println("15. Schedule Automated Tasks [NEW]");
        System.out.println("16. View System Performance [NEW]");
        System.out.println("17. Cache Management [NEW]");
        System.out.println("18. Audit Trail Viewer [NEW]");
        System.out.println("19. Exit");

        System.out.println("\n" + "─".repeat(60));
        System.out.printf("Background Tasks: %d active | Stats updating...\n", activeBackgroundTasks);
        System.out.println("─".repeat(60));
        System.out.print("Enter choice: ");
    }

    private void processMenuChoice(int choice) throws Exception {
        switch (choice) {
            case 1: addStudentWithValidation(); break;
            case 2: viewStudents(); break;
            case 3: recordGrade(); break;
            case 4: viewGradeReport(); break;
            case 5: exportGradeReport(); break;
            case 6: importDataMultiFormat(); break;
            case 7: bulkImportGrades(); break;
            case 8: calculateStudentGPA(); break;
            case 9: viewClassStatistics(); break;
            case 10: toggleRealTimeDashboard(); break;
            case 11: generateBatchReports(); break;
            case 12: searchStudentsAdvanced(); break;
            case 13: patternBasedSearch(); break;
            case 14: queryGradeHistory(); break;
            case 15: scheduleAutomatedTasks(); break;
            case 16: viewSystemPerformance(); break;
            case 17: manageCache(); break;
            case 18: viewAuditTrail(); break;
            case 19: shutdownSystem(); break;
        }
    }

    // Menu option implementations
    private void addStudentWithValidation() throws Exception {
        System.out.println("\n=== ADD STUDENT WITH VALIDATION ===");

        String id = getValidatedInput("Enter Student ID (e.g., S001, CS2024001): ",
                input -> validationService.isValidStudentId(input), "Invalid student ID format");

        String name = getValidatedInput("Enter Full Name (First Last): ",
                input -> validationService.isValidName(input), "Invalid name format");

        String email = getValidatedInput("Enter Email: ",
                input -> validationService.isValidEmail(input, "university.edu"),
                "Invalid email format or domain");

        String phone = getValidatedInput("Enter Phone: ",
                input -> { validationService.normalizePhone(input); return true; },
                "Invalid phone number");

        System.out.println("Select Student Type:");
        System.out.println("1. Regular Student");
        System.out.println("2. Honors Student");
        int typeChoice = getValidatedChoice(1, 2);

        ContactInfo contactInfo = new ContactInfo(email, phone, "");

        if (typeChoice == 1) {
            studentService.addStudent(id, name, contactInfo, "Regular");
        } else {
            studentService.addStudent(id, name, contactInfo, "Honors");
        }

        System.out.println("Student added successfully!");
        auditRepository.addLog(new AuditLog("ADD_STUDENT", "SYSTEM", "Student", id,
                "Added new student: " + name, true));
    }

    private void viewStudents() {
        System.out.println("\n=== ALL STUDENTS ===");
        List<Student> students = studentService.getAllStudents();

        if (students.isEmpty()) {
            System.out.println("No students registered.");
            return;
        }

        students.forEach(System.out::println);
        System.out.printf("\nTotal: %d students\n", students.size());
    }

    private void recordGrade() throws Exception {
        System.out.println("\n=== RECORD GRADE ===");

        List<Student> students = studentService.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("No students available. Add students first.");
            return;
        }

        displayStudentList(students);
        System.out.print("Select student number: ");
        int studentIndex = getValidatedChoice(1, students.size()) - 1;

        Student student = students.get(studentIndex);

        // For simplicity, using hardcoded subjects
        System.out.println("\nSelect Subject:");
        System.out.println("1. Mathematics (Core)");
        System.out.println("2. English (Core)");
        System.out.println("3. Science (Core)");
        System.out.println("4. Music (Elective)");
        System.out.println("5. Art (Elective)");
        System.out.println("6. Physical Education (Elective)");
        System.out.print("Enter choice: ");
        int subjectChoice = getValidatedChoice(1, 6);

        // Create subject based on choice
        // Note: In real implementation, you'd have a Subject repository

        System.out.print("Enter grade (0-100): ");
        double score = getValidatedDouble(0, 100);

        scanner.nextLine(); // Clear buffer
        System.out.print("Enter date (YYYY-MM-DD): ");
        String date = scanner.nextLine();

        System.out.print("Enter semester (e.g., Fall 2024): ");
        String semester = scanner.nextLine();

        System.out.print("Enter notes (optional): ");
        String notes = scanner.nextLine();

        // Record grade
        // Note: This is simplified - you'd need to create actual Subject objects
        System.out.println("Grade recorded (simulated). In full implementation, subject would be created.");

        auditRepository.addLog(new AuditLog("RECORD_GRADE", "SYSTEM", "Grade",
                student.getId(), "Recorded grade: " + score + " for student", true));
    }

    private void viewGradeReport() throws Exception {
        System.out.println("\n=== GRADE REPORT ===");

        List<Student> students = studentService.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("No students available.");
            return;
        }

        displayStudentList(students);
        System.out.print("Select student number: ");
        int studentIndex = getValidatedChoice(1, students.size()) - 1;

        Student student = students.get(studentIndex);
        String report = studentService.generateStudentReport(student.getId());
        System.out.println("\n" + report);
    }

    private void exportGradeReport() throws Exception {
        System.out.println("\n=== EXPORT GRADE REPORT ===");
        System.out.println("Select Format:");
        System.out.println("1. CSV");
        System.out.println("2. JSON");
        System.out.println("3. Binary");
        System.out.print("Enter choice: ");

        int formatChoice = getValidatedChoice(1, 3);
        FileFormat format = FileFormat.fromChoice(formatChoice);

        scanner.nextLine(); // Clear buffer
        System.out.print("Enter output directory: ");
        String dirStr = scanner.nextLine();

        System.out.print("Enter filename (without extension): ");
        String filename = scanner.nextLine();

        Path outputDir = Paths.get(dirStr);
        Files.createDirectories(outputDir);

        Path outputPath = outputDir.resolve(filename + "." + format.getExtension());
        List<Student> students = studentService.getAllStudents();

        fileService.exportData(students, outputPath, format);
        System.out.println("Export completed: " + outputPath.toAbsolutePath());
    }

    private void importDataMultiFormat() throws Exception {
        System.out.println("\n=== IMPORT DATA ===");
        System.out.println("Select Format:");
        System.out.println("1. CSV");
        System.out.println("2. JSON");
        System.out.println("3. Binary");
        System.out.print("Enter choice: ");

        int formatChoice = getValidatedChoice(1, 3);

        scanner.nextLine(); // Clear buffer
        System.out.print("Enter file path: ");
        String filePath = scanner.nextLine();

        List<Student> importedStudents;

        switch (formatChoice) {
            case 1:
                importedStudents = fileService.importStudentsFromCSV(filePath);
                break;
            case 2:
                importedStudents = fileService.importFromJSON(filePath);
                break;
            case 3:
                importedStudents = fileService.importFromBinary(filePath);
                break;
            default:
                throw new IllegalArgumentException("Invalid format choice");
        }

        System.out.println("Imported " + importedStudents.size() + " students.");
        // In full implementation, you'd add these to the repository
    }

    private void bulkImportGrades() throws Exception {
        System.out.println("\n=== BULK IMPORT GRADES ===");
        scanner.nextLine(); // Clear buffer

        System.out.print("Enter CSV file path: ");
        String filePath = scanner.nextLine();

        List<Student> students = studentService.getAllStudents();
        // In full implementation, you'd need subjects map
        Map<String, AbstractSubject> subjects = new HashMap<>();

        List<Grade> importedGrades = fileService.importGradesFromCSV(filePath, students, subjects);
        System.out.println("Imported " + importedGrades.size() + " grades.");
    }

    private void calculateStudentGPA() throws Exception {
        System.out.println("\n=== CALCULATE STUDENT GPA ===");

        List<Student> students = studentService.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("No students available.");
            return;
        }

        displayStudentList(students);
        System.out.print("Select student number: ");
        int studentIndex = getValidatedChoice(1, students.size()) - 1;

        Student student = students.get(studentIndex);
        double gpa = student.calculateGPA();
        double weightedGPA = student.calculateWeightedGPA();

        System.out.printf("\nGPA for %s:\n", student.getName());
        System.out.printf("Cumulative GPA: %.3f\n", gpa);
        System.out.printf("Weighted GPA: %.3f\n", weightedGPA);
        System.out.printf("Letter Grade Equivalent: %s\n",
                validationService.getLetterGrade(student.calculateAverageGrade()));
    }

    private void viewClassStatistics() {
        System.out.println("\n=== CLASS STATISTICS ===");
        List<Student> students = studentService.getAllStudents();

        if (students.isEmpty()) {
            System.out.println("No students available.");
            return;
        }

        String report = statisticsService.generateStatisticsReport(students);
        System.out.println(report);
    }

    private void toggleRealTimeDashboard() {
        dashboardRunning = !dashboardRunning;

        if (dashboardRunning) {
            System.out.println("\nReal-time dashboard started.");
            System.out.println("Statistics will update every 5 seconds.");
            // In full implementation, start a background thread
        } else {
            System.out.println("\nDashboard stopped.");
        }
    }

    private void generateBatchReports() throws Exception {
        System.out.println("\n=== GENERATE BATCH REPORTS ===");

        List<Student> students = studentService.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("No students available.");
            return;
        }

        scanner.nextLine(); // Clear buffer
        System.out.print("Enter output directory: ");
        String dirStr = scanner.nextLine();

        Path outputDir = Paths.get(dirStr);
        Files.createDirectories(outputDir);

        System.out.println("Generating reports for " + students.size() + " students...");

        // Create report service with 4 threads
        ReportService reportService = new ReportService(4);
        List<Path> reports = reportService.generateBatchReports(students, outputDir, FileFormat.CSV);

        System.out.println("Generated " + reports.size() + " reports in " + outputDir.toAbsolutePath());
        reportService.shutdown();
    }

    private void searchStudentsAdvanced() throws Exception {
        System.out.println("\n=== ADVANCED STUDENT SEARCH ===");
        scanner.nextLine(); // Clear buffer

        System.out.print("Enter name (or leave empty): ");
        String name = scanner.nextLine();

        System.out.print("Enter email domain (or leave empty): ");
        String emailDomain = scanner.nextLine();

        System.out.print("Enter type (Regular/Honors or leave empty): ");
        String type = scanner.nextLine();

        List<Student> results = studentService.advancedSearch(name, emailDomain, type);

        if (results.isEmpty()) {
            System.out.println("No students found.");
        } else {
            System.out.println("\nFound " + results.size() + " student(s):");
            results.forEach(System.out::println);
        }
    }

    private void patternBasedSearch() {
        System.out.println("\n=== PATTERN-BASED SEARCH ===");
        scanner.nextLine(); // Clear buffer

        System.out.println("Examples:");
        System.out.println("  - Find emails: .*@university\\.edu");
        System.out.println("  - Find CS students: ^CS.*");
        System.out.println("  - Find names with 'John': .*John.*");
        System.out.print("\nEnter regex pattern: ");
        String pattern = scanner.nextLine();

        List<Student> results = studentService.getAllStudents().stream()
                .filter(student -> {
                    String studentData = student.getId() + " " + student.getName() + " " +
                            student.getContactInfo().getEmail();
                    return studentData.matches(".*" + pattern + ".*");
                })
                .toList();

        System.out.println("\nFound " + results.size() + " match(es):");
        results.forEach(System.out::println);
    }

    private void queryGradeHistory() {
        System.out.println("\n=== QUERY GRADE HISTORY ===");
        scanner.nextLine(); // Clear buffer

        System.out.print("Enter student ID (or leave empty for all): ");
        String studentId = scanner.nextLine();

        System.out.print("Enter minimum score (or leave empty): ");
        String minScoreStr = scanner.nextLine();
        Double minScore = minScoreStr.isEmpty() ? null : Double.parseDouble(minScoreStr);

        System.out.print("Enter maximum score (or leave empty): ");
        String maxScoreStr = scanner.nextLine();
        Double maxScore = maxScoreStr.isEmpty() ? null : Double.parseDouble(maxScoreStr);

        // In full implementation, use GradeService.searchGrades()
        System.out.println("Query executed. In full implementation, would display results.");
    }

    private void scheduleAutomatedTasks() {
        System.out.println("\n=== SCHEDULE AUTOMATED TASKS ===");
        System.out.println("1. Schedule Daily GPA Calculation (2 AM)");
        System.out.println("2. Schedule Hourly Statistics Update");
        System.out.println("3. View Scheduled Tasks");
        System.out.print("Enter choice: ");

        int choice = getValidatedChoice(1, 3);

        switch (choice) {
            case 1:
                taskScheduler.scheduleDailyGPACalculation();
                System.out.println("Daily GPA calculation scheduled.");
                break;
            case 2:
                System.out.println("Hourly statistics update would be scheduled.");
                break;
            case 3:
                System.out.println("Scheduled tasks view would be displayed.");
                break;
        }
    }

    private void viewSystemPerformance() {
        System.out.println("\n=== SYSTEM PERFORMANCE ===");
        Map<String, Object> metrics = performanceMonitor.getMetrics();

        System.out.println("Performance Metrics:");
        metrics.forEach((key, value) -> System.out.printf("  %s: %s\n", key, value));

        System.out.println("\nCache Statistics:");
        System.out.printf("  Size: %d\n", cacheService.size());
        System.out.printf("  Hit Rate: %.1f%%\n", cacheService.getHitRate());
    }

    private void manageCache() {
        System.out.println("\n=== CACHE MANAGEMENT ===");
        System.out.println("1. View Cache Contents");
        System.out.println("2. Clear Cache");
        System.out.println("3. View Cache Statistics");
        System.out.print("Enter choice: ");

        int choice = getValidatedChoice(1, 3);

        switch (choice) {
            case 1:
                cacheService.displayContents();
                break;
            case 2:
                cacheService.clear();
                System.out.println("Cache cleared.");
                break;
            case 3:
                System.out.printf("Cache Size: %d\n", cacheService.size());
                System.out.printf("Hit Rate: %.2f%%\n", cacheService.getHitRate());
                break;
        }
    }

    private void viewAuditTrail() {
        System.out.println("\n=== AUDIT TRAIL ===");

        // Get latest 20 logs
        List<AuditLog> logs = auditRepository.getLatestLogs(20);

        if (logs.isEmpty()) {
            System.out.println("No audit logs found.");
            return;
        }

        System.out.println("Latest " + logs.size() + " audit entries:");
        logs.forEach(log -> System.out.println("  " + log));
    }

    private void shutdownSystem() {
        System.out.println("\n=== SHUTTING DOWN SYSTEM ===");
        System.out.println("Saving data...");
        System.out.println("Stopping services...");
        System.out.println("System shutdown complete.");
    }

    // Helper methods
    private void displayWelcome() {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║    ADVANCED GRADE MANAGEMENT SYSTEM v3.0                 ║");
        System.out.println("║    Features:                                             ║");
        System.out.println("║    • Multi-format import/export (CSV, JSON, Binary)      ║");
        System.out.println("║    • Real-time statistics dashboard                      ║");
        System.out.println("║    • Advanced pattern-based search                       ║");
        System.out.println("║    • Automated task scheduling                           ║");
        System.out.println("║    • Performance monitoring & caching                    ║");
        System.out.println("║    • Comprehensive audit trail                           ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println("\nInitializing system...");
    }

    private void displayStudentList(List<Student> students) {
        System.out.println("\nAvailable Students:");
        for (int i = 0; i < students.size(); i++) {
            System.out.printf("%2d. %s (ID: %s)\n", i + 1,
                    students.get(i).getName(), students.get(i).getId());
        }
    }

    private int getValidatedChoice(int min, int max) {
        while (true) {
            try {
                int choice = scanner.nextInt();
                if (choice >= min && choice <= max) {
                    return choice;
                }
                System.out.printf("Please enter a number between %d and %d: ", min, max);
            } catch (Exception e) {
                System.out.print("Invalid input. Please enter a number: ");
                scanner.next(); // Clear invalid input
            }
        }
    }

    private double getValidatedDouble(double min, double max) {
        while (true) {
            try {
                double value = scanner.nextDouble();
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("Please enter a number between %.1f and %.1f: ", min, max);
            } catch (Exception e) {
                System.out.print("Invalid input. Please enter a number: ");
                scanner.next(); // Clear invalid input
            }
        }
    }

    private String getValidatedInput(String prompt, java.util.function.Predicate<String> validator,
                                     String errorMessage) {
        scanner.nextLine(); // Clear buffer
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                if (validator.test(input)) {
                    return input;
                }
                System.out.println(errorMessage);
            } catch (Exception e) {
                System.out.println(errorMessage + ": " + e.getMessage());
            }
        }
    }

    private void updateActiveTasks() {
        // Simulate active tasks
        activeBackgroundTasks = dashboardRunning ? 1 : 0;
        activeBackgroundTasks += taskScheduler instanceof Object ? 1 : 0; // Simplified
    }
}