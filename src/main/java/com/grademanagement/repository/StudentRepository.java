package com.grademanagement.repository;

import com.grademanagement.model.Student;
import com.grademanagement.model.Grade;
import com.grademanagement.model.HonorsStudent;
import com.grademanagement.model.RegularStudent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

// Repository using optimized collections for performance
public class StudentRepository {
    // HashMap for O(1) student lookup by ID
    private final Map<String, Student> studentsById;

    // TreeMap for sorted student display (sorted by ID)
    private final SortedMap<String, Student> studentsSorted;

    // Concurrent collections for thread safety
    private final Map<String, List<Grade>> gradesByStudentId;

    // Indexes for faster searches
    private final Map<String, Set<Student>> studentsByNameIndex;
    private final Map<String, Set<Student>> studentsByEmailIndex;
    private final Map<String, Set<Student>> studentsByTypeIndex;

    // Statistics cache
    private final Map<String, Object> statisticsCache;
    private long lastStatisticsUpdate;
    private static final long STATS_CACHE_TIMEOUT = 30000; // 30 seconds

    public StudentRepository() {
        // HashMap for fastest lookups (O(1) average)
        this.studentsById = new ConcurrentHashMap<>();

        // TreeMap for sorted operations (O(log n))
        this.studentsSorted = new ConcurrentSkipListMap<>();

        // Concurrent collections for thread-safe operations
        this.gradesByStudentId = new ConcurrentHashMap<>();

        // Indexes for advanced searches
        this.studentsByNameIndex = new ConcurrentHashMap<>();
        this.studentsByEmailIndex = new ConcurrentHashMap<>();
        this.studentsByTypeIndex = new ConcurrentHashMap<>();

        // Initialize statistics cache
        this.statisticsCache = new ConcurrentHashMap<>();
        this.lastStatisticsUpdate = 0;

        // Initialize type index
        this.studentsByTypeIndex.put("Regular", ConcurrentHashMap.newKeySet());
        this.studentsByTypeIndex.put("Honors", ConcurrentHashMap.newKeySet());
    }

    // Add student with indexing
    public void addStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null");
        }

        String id = student.getId();

        // Check if student already exists
        if (studentsById.containsKey(id)) {
            throw new IllegalArgumentException("Student with ID " + id + " already exists");
        }

        // Store in main collections
        studentsById.put(id, student);
        studentsSorted.put(id, student);

        // Update indexes
        addToNameIndex(student);
        addToEmailIndex(student);
        addToTypeIndex(student);

        // Initialize grades list
        gradesByStudentId.putIfAbsent(id, new ArrayList<>());

        // Invalidate statistics cache
        invalidateStatisticsCache();
    }

    // O(1) lookup by ID
    public Student findById(String id) {
        if (id == null) return null;
        return studentsById.get(id);
    }

    // Check if student exists
    public boolean exists(String id) {
        return studentsById.containsKey(id);
    }

    // Get all students
    public List<Student> getAllStudents() {
        return new ArrayList<>(studentsById.values());
    }

    // Get students sorted by ID
    public List<Student> getAllStudentsSorted() {
        return new ArrayList<>(studentsSorted.values());
    }

    // O(log n) range queries
    public List<Student> findByIdRange(String fromId, String toId) {
        return new ArrayList<>(studentsSorted.subMap(fromId, toId).values());
    }

    // Fast search by name using index (partial matching)
    public List<Student> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String searchTerm = name.toLowerCase().trim();
        Set<Student> results = ConcurrentHashMap.newKeySet();

        // Search through name index
        for (Map.Entry<String, Set<Student>> entry : studentsByNameIndex.entrySet()) {
            if (entry.getKey().contains(searchTerm)) {
                results.addAll(entry.getValue());
            }
        }

        return new ArrayList<>(results);
    }

    // Exact name match
    public List<Student> findByNameExact(String name) {
        if (name == null) return new ArrayList<>();

        Set<Student> students = studentsByNameIndex.get(name.toLowerCase());
        return students != null ? new ArrayList<>(students) : new ArrayList<>();
    }

    // Fast search by email domain using index
    public List<Student> findByEmailDomain(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Student> result = new ArrayList<>();
        String domainLower = domain.toLowerCase();

        for (Map.Entry<String, Set<Student>> entry : studentsByEmailIndex.entrySet()) {
            String email = entry.getKey();
            int atIndex = email.indexOf('@');

            if (atIndex != -1) {
                String emailDomain = email.substring(atIndex + 1);
                if (emailDomain.equals(domainLower) || emailDomain.endsWith("." + domainLower)) {
                    result.addAll(entry.getValue());
                }
            }
        }

        return result;
    }

    // Search by student type
    public List<Student> findByType(String type) {
        if (type == null) return new ArrayList<>();

        Set<Student> students = studentsByTypeIndex.get(type);
        return students != null ? new ArrayList<>(students) : new ArrayList<>();
    }

    // Get honors students
    public List<HonorsStudent> getHonorsStudents() {
        return studentsById.values().stream()
                .filter(student -> student instanceof HonorsStudent)
                .map(student -> (HonorsStudent) student)
                .collect(Collectors.toList());
    }

    // Get regular students
    public List<RegularStudent> getRegularStudents() {
        return studentsById.values().stream()
                .filter(student -> student instanceof RegularStudent)
                .map(student -> (RegularStudent) student)
                .collect(Collectors.toList());
    }

    // Search by multiple criteria
    public List<Student> search(String name, String emailDomain, String type) {
        Set<Student> resultSet = ConcurrentHashMap.newKeySet();
        boolean firstFilter = true;

        // Apply name filter if provided
        if (name != null && !name.trim().isEmpty()) {
            resultSet.addAll(findByName(name.trim()));
            firstFilter = false;
        }

        // Apply email domain filter if provided
        if (emailDomain != null && !emailDomain.trim().isEmpty()) {
            Set<Student> domainStudents = new HashSet<>(findByEmailDomain(emailDomain.trim()));
            if (firstFilter) {
                resultSet.addAll(domainStudents);
                firstFilter = false;
            } else {
                resultSet.retainAll(domainStudents);
            }
        }

        // Apply type filter if provided
        if (type != null && !type.trim().isEmpty()) {
            Set<Student> typeStudents = new HashSet<>(findByType(type.trim()));
            if (firstFilter) {
                resultSet.addAll(typeStudents);
            } else {
                resultSet.retainAll(typeStudents);
            }
        }

        return new ArrayList<>(resultSet);
    }

    // Update student information
    public boolean updateStudent(String id, Student updatedStudent) {
        if (id == null || updatedStudent == null) {
            return false;
        }

        if (!studentsById.containsKey(id)) {
            return false;
        }

        // Remove old indexes
        Student oldStudent = studentsById.get(id);
        removeFromIndexes(oldStudent);

        // Update in main collections
        studentsById.put(id, updatedStudent);
        studentsSorted.put(id, updatedStudent);

        // Add new indexes
        addToNameIndex(updatedStudent);
        addToEmailIndex(updatedStudent);
        addToTypeIndex(updatedStudent);

        // Invalidate statistics cache
        invalidateStatisticsCache();

        return true;
    }

    // Delete student
    public boolean deleteStudent(String id) {
        if (id == null || !studentsById.containsKey(id)) {
            return false;
        }

        Student student = studentsById.remove(id);
        studentsSorted.remove(id);

        // Remove from indexes
        removeFromIndexes(student);

        // Remove grades
        gradesByStudentId.remove(id);

        // Invalidate statistics cache
        invalidateStatisticsCache();

        return true;
    }

    // Add grades with fast lookup
    public void addGrade(String studentId, Grade grade) {
        if (studentId == null || grade == null) {
            throw new IllegalArgumentException("Student ID and grade cannot be null");
        }

        List<Grade> grades = gradesByStudentId.get(studentId);
        if (grades != null) {
            grades.add(grade);
        } else {
            throw new IllegalArgumentException("Student not found: " + studentId);
        }

        // Invalidate statistics cache
        invalidateStatisticsCache();
    }

    // Get grades with optional sorting
    public List<Grade> getGrades(String studentId, boolean sortByDate) {
        List<Grade> grades = gradesByStudentId.get(studentId);
        if (grades == null) return new ArrayList<>();

        if (sortByDate) {
            List<Grade> sorted = new ArrayList<>(grades);
            sorted.sort(Comparator.comparing(Grade::getDate));
            return sorted;
        }
        return new ArrayList<>(grades);
    }

    // Get all grades across all students
    public List<Grade> getAllGrades() {
        List<Grade> allGrades = new ArrayList<>();
        for (List<Grade> grades : gradesByStudentId.values()) {
            allGrades.addAll(grades);
        }
        return allGrades;
    }

    // Indexing methods
    private void addToNameIndex(Student student) {
        String nameKey = student.getName().toLowerCase();
        studentsByNameIndex
                .computeIfAbsent(nameKey, k -> ConcurrentHashMap.newKeySet())
                .add(student);
    }

    private void addToEmailIndex(Student student) {
        if (student.getContactInfo() != null && student.getContactInfo().getEmail() != null) {
            String email = student.getContactInfo().getEmail().toLowerCase();
            studentsByEmailIndex
                    .computeIfAbsent(email, k -> ConcurrentHashMap.newKeySet())
                    .add(student);
        }
    }

    private void addToTypeIndex(Student student) {
        String type = student.getStudentType();
        studentsByTypeIndex
                .computeIfAbsent(type, k -> ConcurrentHashMap.newKeySet())
                .add(student);
    }

    private void removeFromIndexes(Student student) {
        // Remove from name index
        String nameKey = student.getName().toLowerCase();
        Set<Student> nameSet = studentsByNameIndex.get(nameKey);
        if (nameSet != null) {
            nameSet.remove(student);
            if (nameSet.isEmpty()) {
                studentsByNameIndex.remove(nameKey);
            }
        }

        // Remove from email index
        if (student.getContactInfo() != null && student.getContactInfo().getEmail() != null) {
            String email = student.getContactInfo().getEmail().toLowerCase();
            Set<Student> emailSet = studentsByEmailIndex.get(email);
            if (emailSet != null) {
                emailSet.remove(student);
                if (emailSet.isEmpty()) {
                    studentsByEmailIndex.remove(email);
                }
            }
        }

        // Remove from type index
        String type = student.getStudentType();
        Set<Student> typeSet = studentsByTypeIndex.get(type);
        if (typeSet != null) {
            typeSet.remove(student);
            if (typeSet.isEmpty()) {
                studentsByTypeIndex.remove(type);
            }
        }
    }

    private void invalidateStatisticsCache() {
        lastStatisticsUpdate = 0;
        statisticsCache.clear();
    }

    private void updateStatisticsCache() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastStatisticsUpdate < STATS_CACHE_TIMEOUT && !statisticsCache.isEmpty()) {
            return; // Cache is still valid
        }

        // Calculate statistics
        Map<String, Object> stats = new HashMap<>();

        // Basic counts
        stats.put("totalStudents", studentsById.size());
        stats.put("totalGrades", gradesByStudentId.values().stream()
                .mapToInt(List::size).sum());

        // Type distribution
        Map<String, Integer> typeDistribution = new HashMap<>();
        for (Map.Entry<String, Set<Student>> entry : studentsByTypeIndex.entrySet()) {
            typeDistribution.put(entry.getKey(), entry.getValue().size());
        }
        stats.put("typeDistribution", typeDistribution);

        // Grade statistics
        List<Grade> allGrades = getAllGrades();
        if (!allGrades.isEmpty()) {
            double averageGrade = allGrades.stream()
                    .mapToDouble(Grade::getScore)
                    .average()
                    .orElse(0.0);
            stats.put("averageGrade", averageGrade);

            double highestGrade = allGrades.stream()
                    .mapToDouble(Grade::getScore)
                    .max()
                    .orElse(0.0);
            stats.put("highestGrade", highestGrade);

            double lowestGrade = allGrades.stream()
                    .mapToDouble(Grade::getScore)
                    .min()
                    .orElse(0.0);
            stats.put("lowestGrade", lowestGrade);
        } else {
            stats.put("averageGrade", 0.0);
            stats.put("highestGrade", 0.0);
            stats.put("lowestGrade", 0.0);
        }

        // Index sizes
        stats.put("nameIndexSize", studentsByNameIndex.size());
        stats.put("emailIndexSize", studentsByEmailIndex.size());
        stats.put("typeIndexSize", studentsByTypeIndex.size());

        // Update cache
        statisticsCache.putAll(stats);
        lastStatisticsUpdate = currentTime;
    }

    // Performance metrics
    public Map<String, Object> getRepositoryMetrics() {
        updateStatisticsCache(); // Ensure cache is up to date
        return new HashMap<>(statisticsCache);
    }

    // Get detailed statistics
    public String getDetailedStatistics() {
        Map<String, Object> metrics = getRepositoryMetrics();
        StringBuilder stringbuilder1 = new StringBuilder();

        stringbuilder1.append("=== REPOSITORY STATISTICS ===\n");
        stringbuilder1.append(String.format("Total Students: %d\n", metrics.get("totalStudents")));
        stringbuilder1.append(String.format("Total Grades: %d\n", metrics.get("totalGrades")));

        Map<String, Integer> typeDist = (Map<String, Integer>) metrics.get("typeDistribution");
        stringbuilder1.append("Student Type Distribution:\n");
        for (Map.Entry<String, Integer> entry : typeDist.entrySet()) {
            stringbuilder1.append(String.format("  - %s: %d\n", entry.getKey(), entry.getValue()));
        }

        stringbuilder1.append(String.format("Average Grade: %.2f\n", metrics.get("averageGrade")));
        stringbuilder1.append(String.format("Highest Grade: %.2f\n", metrics.get("highestGrade")));
        stringbuilder1.append(String.format("Lowest Grade: %.2f\n", metrics.get("lowestGrade")));

        stringbuilder1.append(String.format("Name Index Size: %d\n", metrics.get("nameIndexSize")));
        stringbuilder1.append(String.format("Email Index Size: %d\n", metrics.get("emailIndexSize")));
        stringbuilder1.append(String.format("Type Index Size: %d\n", metrics.get("typeIndexSize")));

        return stringbuilder1.toString();
    }

    // Get count of active students
    public long getActiveStudentCount() {
        return studentsById.values().stream()
                .filter(Student::isActive)
                .count();
    }

    // Get students with GPA above threshold
    public List<Student> getStudentsWithGpaAbove(double threshold) {
        return studentsById.values().stream()
                .filter(student -> student.calculateGPA() >= threshold)
                .collect(Collectors.toList());
    }

    // Get students with average grade above threshold
    public List<Student> getStudentsWithAverageAbove(double threshold) {
        return studentsById.values().stream()
                .filter(student -> student.calculateAverageGrade() >= threshold)
                .collect(Collectors.toList());
    }

    // Export repository state for backup
    public Map<String, Object> exportState() {
        Map<String, Object> state = new HashMap<>();
        state.put("students", new ArrayList<>(studentsById.values()));
        state.put("gradesByStudent", new HashMap<>(gradesByStudentId));
        state.put("timestamp", System.currentTimeMillis());
        return state;
    }

    // Import repository state from backup
    public void importState(Map<String, Object> state) {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }

        // Clear existing data
        studentsById.clear();
        studentsSorted.clear();
        gradesByStudentId.clear();
        studentsByNameIndex.clear();
        studentsByEmailIndex.clear();
        studentsByTypeIndex.clear();

        // Import students
        List<Student> studentList = (List<Student>) state.get("students");
        if (studentList != null) {
            for (Student student : studentList) {
                addStudent(student);
            }
        }

        // Import grades
        Map<String, List<Grade>> gradesMap = (Map<String, List<Grade>>) state.get("gradesByStudent");
        if (gradesMap != null) {
            gradesByStudentId.putAll(gradesMap);
        }

        // Invalidate cache
        invalidateStatisticsCache();
    }
}
