package com.grademanagement.repository;

import com.grademanagement.model.Grade;
import com.grademanagement.model.Student;
import com.grademanagement.model.subjects.AbstractSubject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

// Enhanced grade repository with concurrent operations
public class GradeRepository {
    // Primary storage: Grade ID -> Grade
    private final Map<String, Grade> gradesById;

    // Secondary indexes for fast lookups
    private final Map<String, List<Grade>> gradesByStudentId;
    private final Map<String, List<Grade>> gradesBySubject;
    private final SortedMap<String, List<Grade>> gradesByDate;
    private final Map<String, List<Grade>> gradesBySemester;

    // Statistics cache
    private final Map<String, Object> statisticsCache;
    private long lastStatisticsUpdate;

    // ID generator
    private long nextGradeId = 1;

    public GradeRepository() {
        this.gradesById = new ConcurrentHashMap<>();
        this.gradesByStudentId = new ConcurrentHashMap<>();
        this.gradesBySubject = new ConcurrentHashMap<>();
        this.gradesByDate = new ConcurrentSkipListMap<>(Collections.reverseOrder());
        this.gradesBySemester = new ConcurrentHashMap<>();
        this.statisticsCache = new ConcurrentHashMap<>();
        this.lastStatisticsUpdate = 0;
    }

    // Generate unique grade ID
    private synchronized String generateGradeId() {
        return "GRADE_" + System.currentTimeMillis() + "_" + (nextGradeId++);
    }

    // Add a grade with automatic ID generation
    public String addGrade(Grade grade) {
        if (grade == null) {
            throw new IllegalArgumentException("Grade cannot be null");
        }

        String gradeId = generateGradeId();
        return addGrade(gradeId, grade);
    }

    // Add a grade with specified ID
    public String addGrade(String gradeId, Grade grade) {
        if (gradeId == null || gradeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Grade ID cannot be null or empty");
        }
        if (grade == null) {
            throw new IllegalArgumentException("Grade cannot be null");
        }

        // Check if grade already exists
        if (gradesById.containsKey(gradeId)) {
            throw new IllegalArgumentException("Grade with ID " + gradeId + " already exists");
        }

        // Store in primary map
        gradesById.put(gradeId, grade);

        // Update all indexes
        updateIndexes(gradeId, grade);

        // Invalidate statistics cache
        invalidateStatisticsCache();

        return gradeId;
    }

    // Update indexes for a grade
    private void updateIndexes(String gradeId, Grade grade) {
        Student student = grade.getStudent();
        AbstractSubject subject = grade.getSubject();
        String date = grade.getDate();
        String semester = grade.getSemester();

        // Index by student ID
        gradesByStudentId
                .computeIfAbsent(student.getId(), k -> new ArrayList<>())
                .add(grade);

        // Index by subject
        gradesBySubject
                .computeIfAbsent(subject.getName(), k -> new ArrayList<>())
                .add(grade);

        // Index by date
        gradesByDate
                .computeIfAbsent(date, k -> new ArrayList<>())
                .add(grade);

        // Index by semester (if provided)
        if (semester != null && !semester.trim().isEmpty()) {
            gradesBySemester
                    .computeIfAbsent(semester, k -> new ArrayList<>())
                    .add(grade);
        }
    }

    // Get grade by ID
    public Grade getGrade(String gradeId) {
        return gradesById.get(gradeId);
    }

    // Check if grade exists
    public boolean exists(String gradeId) {
        return gradesById.containsKey(gradeId);
    }

    // Get all grades
    public List<Grade> getAllGrades() {
        return new ArrayList<>(gradesById.values());
    }

    // Get grades by student ID
    public List<Grade> getGradesByStudentId(String studentId) {
        List<Grade> grades = gradesByStudentId.get(studentId);
        return grades != null ? new ArrayList<>(grades) : new ArrayList<>();
    }

    // Get grades by subject
    public List<Grade> getGradesBySubject(String subjectName) {
        List<Grade> grades = gradesBySubject.get(subjectName);
        return grades != null ? new ArrayList<>(grades) : new ArrayList<>();
    }

    // Get grades by date range
    public List<Grade> getGradesByDateRange(String fromDate, String toDate) {
        List<Grade> result = new ArrayList<>();
        SortedMap<String, List<Grade>> subMap = gradesByDate.subMap(fromDate, toDate);

        for (List<Grade> gradeList : subMap.values()) {
            result.addAll(gradeList);
        }

        return result;
    }

    // Get grades by semester
    public List<Grade> getGradesBySemester(String semester) {
        List<Grade> grades = gradesBySemester.get(semester);
        return grades != null ? new ArrayList<>(grades) : new ArrayList<>();
    }

    // Get latest N grades
    public List<Grade> getLatestGrades(int n) {
        List<Grade> result = new ArrayList<>();
        int count = 0;

        for (List<Grade> gradeList : gradesByDate.values()) {
            for (Grade grade : gradeList) {
                if (count >= n) break;
                result.add(grade);
                count++;
            }
            if (count >= n) break;
        }

        return result;
    }

    // Update a grade
    public boolean updateGrade(String gradeId, Grade updatedGrade) {
        if (gradeId == null || updatedGrade == null) {
            return false;
        }

        if (!gradesById.containsKey(gradeId)) {
            return false;
        }

        // Remove old indexes
        Grade oldGrade = gradesById.get(gradeId);
        removeFromIndexes(gradeId, oldGrade);

        // Update in primary map
        gradesById.put(gradeId, updatedGrade);

        // Add new indexes
        updateIndexes(gradeId, updatedGrade);

        // Invalidate statistics cache
        invalidateStatisticsCache();

        return true;
    }

    // Remove from indexes
    private void removeFromIndexes(String gradeId, Grade grade) {
        Student student = grade.getStudent();
        AbstractSubject subject = grade.getSubject();
        String date = grade.getDate();
        String semester = grade.getSemester();

        // Remove from student index
        List<Grade> studentGrades = gradesByStudentId.get(student.getId());
        if (studentGrades != null) {
            studentGrades.remove(grade);
            if (studentGrades.isEmpty()) {
                gradesByStudentId.remove(student.getId());
            }
        }

        // Remove from subject index
        List<Grade> subjectGrades = gradesBySubject.get(subject.getName());
        if (subjectGrades != null) {
            subjectGrades.remove(grade);
            if (subjectGrades.isEmpty()) {
                gradesBySubject.remove(subject.getName());
            }
        }

        // Remove from date index
        List<Grade> dateGrades = gradesByDate.get(date);
        if (dateGrades != null) {
            dateGrades.remove(grade);
            if (dateGrades.isEmpty()) {
                gradesByDate.remove(date);
            }
        }

        // Remove from semester index
        if (semester != null && !semester.trim().isEmpty()) {
            List<Grade> semesterGrades = gradesBySemester.get(semester);
            if (semesterGrades != null) {
                semesterGrades.remove(grade);
                if (semesterGrades.isEmpty()) {
                    gradesBySemester.remove(semester);
                }
            }
        }
    }

    // Delete a grade
    public boolean deleteGrade(String gradeId) {
        if (gradeId == null || !gradesById.containsKey(gradeId)) {
            return false;
        }

        Grade grade = gradesById.remove(gradeId);
        removeFromIndexes(gradeId, grade);

        // Invalidate statistics cache
        invalidateStatisticsCache();

        return true;
    }

    // Delete all grades for a student
    public int deleteGradesForStudent(String studentId) {
        List<Grade> grades = getGradesByStudentId(studentId);
        int count = 0;

        for (Grade grade : grades) {
            // Find the grade ID
            for (Map.Entry<String, Grade> entry : gradesById.entrySet()) {
                if (entry.getValue() == grade) {
                    deleteGrade(entry.getKey());
                    count++;
                    break;
                }
            }
        }

        return count;
    }

    // Search grades with multiple criteria
    public List<Grade> searchGrades(String studentId, String subjectName,
                                    String fromDate, String toDate,
                                    Double minScore, Double maxScore) {
        List<Grade> results = new ArrayList<>();

        // Start with all grades
        Collection<Grade> candidates = gradesById.values();

        // Apply filters
        if (studentId != null && !studentId.trim().isEmpty()) {
            candidates = candidates.stream()
                    .filter(grade -> studentId.equals(grade.getStudent().getId()))
                    .collect(Collectors.toList());
        }

        if (subjectName != null && !subjectName.trim().isEmpty()) {
            candidates = candidates.stream()
                    .filter(grade -> subjectName.equals(grade.getSubject().getName()))
                    .collect(Collectors.toList());
        }

        if (fromDate != null && !fromDate.trim().isEmpty()) {
            candidates = candidates.stream()
                    .filter(grade -> grade.getDate().compareTo(fromDate) >= 0)
                    .collect(Collectors.toList());
        }

        if (toDate != null && !toDate.trim().isEmpty()) {
            candidates = candidates.stream()
                    .filter(grade -> grade.getDate().compareTo(toDate) <= 0)
                    .collect(Collectors.toList());
        }

        if (minScore != null) {
            candidates = candidates.stream()
                    .filter(grade -> grade.getScore() >= minScore)
                    .collect(Collectors.toList());
        }

        if (maxScore != null) {
            candidates = candidates.stream()
                    .filter(grade -> grade.getScore() <= maxScore)
                    .collect(Collectors.toList());
        }

        results.addAll(candidates);
        return results;
    }

    // Get statistics for a student
    public Map<String, Object> getStudentStatistics(String studentId) {
        List<Grade> grades = getGradesByStudentId(studentId);
        Map<String, Object> stats = new HashMap<>();

        if (grades.isEmpty()) {
            stats.put("count", 0);
            stats.put("average", 0.0);
            stats.put("highest", 0.0);
            stats.put("lowest", 0.0);
            stats.put("passRate", 0.0);
            return stats;
        }

        double sum = 0;
        double highest = Double.MIN_VALUE;
        double lowest = Double.MAX_VALUE;
        int passCount = 0;

        for (Grade grade : grades) {
            double score = grade.getScore();
            sum += score;
            highest = Math.max(highest, score);
            lowest = Math.min(lowest, score);
            if (grade.isPassing()) {
                passCount++;
            }
        }

        stats.put("count", grades.size());
        stats.put("average", sum / grades.size());
        stats.put("highest", highest);
        stats.put("lowest", lowest);
        stats.put("passRate", (passCount * 100.0) / grades.size());

        // Subject distribution
        Map<String, Long> subjectCount = grades.stream()
                .collect(Collectors.groupingBy(
                        grade -> grade.getSubject().getName(),
                        Collectors.counting()
                ));
        stats.put("subjectDistribution", subjectCount);

        return stats;
    }

    // Get overall statistics
    public Map<String, Object> getOverallStatistics() {
        long currentTime = System.currentTimeMillis();

        // Check cache (valid for 30 seconds)
        if (currentTime - lastStatisticsUpdate < 30000 && !statisticsCache.isEmpty()) {
            return new HashMap<>(statisticsCache);
        }

        Map<String, Object> stats = new HashMap<>();
        List<Grade> allGrades = getAllGrades();

        if (allGrades.isEmpty()) {
            stats.put("totalGrades", 0);
            stats.put("averageGrade", 0.0);
            stats.put("highestGrade", 0.0);
            stats.put("lowestGrade", 0.0);
            stats.put("passRate", 0.0);
            stats.put("gradeDistribution", new HashMap<>());
        } else {
            double sum = 0;
            double highest = Double.MIN_VALUE;
            double lowest = Double.MAX_VALUE;
            int passCount = 0;

            // Grade distribution by letter
            Map<String, Integer> gradeDistribution = new HashMap<>();

            for (Grade grade : allGrades) {
                double score = grade.getScore();
                sum += score;
                highest = Math.max(highest, score);
                lowest = Math.min(lowest, score);

                if (grade.isPassing()) {
                    passCount++;
                }

                // Update grade distribution
                String letter = grade.getLetterGrade().getLetter();
                gradeDistribution.put(letter, gradeDistribution.getOrDefault(letter, 0) + 1);
            }

            stats.put("totalGrades", allGrades.size());
            stats.put("averageGrade", sum / allGrades.size());
            stats.put("highestGrade", highest);
            stats.put("lowestGrade", lowest);
            stats.put("passRate", (passCount * 100.0) / allGrades.size());
            stats.put("gradeDistribution", gradeDistribution);

            // Subject statistics
            Map<String, Double> subjectAverages = new HashMap<>();
            Map<String, Long> subjectCounts = new HashMap<>();

            for (Grade grade : allGrades) {
                String subject = grade.getSubject().getName();
                subjectCounts.put(subject, subjectCounts.getOrDefault(subject, 0L) + 1);

                double currentSum = subjectAverages.getOrDefault(subject, 0.0);
                subjectAverages.put(subject, currentSum + grade.getScore());
            }

            // Calculate averages
            for (Map.Entry<String, Double> entry : subjectAverages.entrySet()) {
                String subject = entry.getKey();
                Long count = subjectCounts.get(subject);
                if (count != null && count > 0) {
                    subjectAverages.put(subject, entry.getValue() / count);
                }
            }

            stats.put("subjectAverages", subjectAverages);
            stats.put("subjectCounts", subjectCounts);
        }

        // Update cache
        statisticsCache.clear();
        statisticsCache.putAll(stats);
        lastStatisticsUpdate = currentTime;

        return new HashMap<>(stats);
    }

    private void invalidateStatisticsCache() {
        lastStatisticsUpdate = 0;
        statisticsCache.clear();
    }

    // Get grade count
    public int getGradeCount() {
        return gradesById.size();
    }

    // Check if empty
    public boolean isEmpty() {
        return gradesById.isEmpty();
    }

    // Clear all grades
    public void clear() {
        gradesById.clear();
        gradesByStudentId.clear();
        gradesBySubject.clear();
        gradesByDate.clear();
        gradesBySemester.clear();
        invalidateStatisticsCache();
    }

    // Export to map for serialization
    public Map<String, Object> exportData() {
        Map<String, Object> data = new HashMap<>();
        data.put("grades", new HashMap<>(gradesById));
        data.put("nextGradeId", nextGradeId);
        data.put("timestamp", System.currentTimeMillis());
        return data;
    }

    // Import from map
    public void importData(Map<String, Object> data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }

        // Clear existing data
        clear();

        // Import grades
        Map<String, Grade> importedGrades = (Map<String, Grade>) data.get("grades");
        if (importedGrades != null) {
            for (Map.Entry<String, Grade> entry : importedGrades.entrySet()) {
                addGrade(entry.getKey(), entry.getValue());
            }
        }

        // Import next grade ID
        Long importedNextId = (Long) data.get("nextGradeId");
        if (importedNextId != null) {
            nextGradeId = importedNextId;
        }
    }
}
