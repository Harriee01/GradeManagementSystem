package com.grademanagement.collections;

import com.grademanagement.model.Grade;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Specialized TreeMap wrapper for grades with enhanced functionality
public class GradeTreeMap {
    // Main storage: TreeMap for sorted grades by date
    private final SortedMap<String, List<Grade>> gradesByDate;

    // Indexes for fast lookups
    private final Map<String, Grade> gradesById; // Grade ID -> Grade
    private final Map<String, List<Grade>> gradesByStudentId; // Student ID -> Grades
    private final Map<String, List<Grade>> gradesBySubject; // Subject -> Grades

    // Lock for thread safety
    private final ReentrantReadWriteLock lock;

    public GradeTreeMap() {
        // Use ConcurrentSkipListMap for thread-safe sorted map
        this.gradesByDate = new ConcurrentSkipListMap<>(Collections.reverseOrder()); // Newest first
        this.gradesById = new ConcurrentHashMap<>();
        this.gradesByStudentId = new ConcurrentHashMap<>();
        this.gradesBySubject = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock(true);
    }

    // Add a grade to all indexes
    public void addGrade(Grade grade, String gradeId) {
        if (grade == null || gradeId == null || gradeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Grade and grade ID cannot be null");
        }

        lock.writeLock().lock();
        try {
            // Add to main storage by date
            String date = grade.getDate();
            gradesByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(grade);

            // Add to ID index
            gradesById.put(gradeId, grade);

            // Add to student index
            String studentId = grade.getStudent().getId();
            gradesByStudentId.computeIfAbsent(studentId, k -> new ArrayList<>()).add(grade);

            // Add to subject index
            String subjectName = grade.getSubject().getName();
            gradesBySubject.computeIfAbsent(subjectName, k -> new ArrayList<>()).add(grade);

        } finally {
            lock.writeLock().unlock();
        }
    }

    // Get grade by ID (O(1))
    public Grade getGradeById(String gradeId) {
        if (gradeId == null) return null;

        lock.readLock().lock();
        try {
            return gradesById.get(gradeId);
        } finally {
            lock.readLock().unlock();
        }
    }

    // Get grades by student ID
    public List<Grade> getGradesByStudentId(String studentId) {
        if (studentId == null) return new ArrayList<>();

        lock.readLock().lock();
        try {
            List<Grade> grades = gradesByStudentId.get(studentId);
            return grades != null ? new ArrayList<>(grades) : new ArrayList<>();
        } finally {
            lock.readLock().unlock();
        }
    }

    // Get grades by date range
    public List<Grade> getGradesByDateRange(String fromDate, String toDate) {
        lock.readLock().lock();
        try {
            List<Grade> result = new ArrayList<>();
            SortedMap<String, List<Grade>> subMap = gradesByDate.subMap(fromDate, toDate);

            for (List<Grade> gradeList : subMap.values()) {
                result.addAll(gradeList);
            }

            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    // Get grades by subject
    public List<Grade> getGradesBySubject(String subjectName) {
        if (subjectName == null) return new ArrayList<>();

        lock.readLock().lock();
        try {
            List<Grade> grades = gradesBySubject.get(subjectName);
            return grades != null ? new ArrayList<>(grades) : new ArrayList<>();
        } finally {
            lock.readLock().unlock();
        }
    }

    // Get latest N grades
    public List<Grade> getLatestGrades(int n) {
        lock.readLock().lock();
        try {
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
        } finally {
            lock.readLock().unlock();
        }
    }

    // Get average grade by student
    public double getAverageGradeByStudent(String studentId) {
        List<Grade> grades = getGradesByStudentId(studentId);
        if (grades.isEmpty()) return 0.0;

        double sum = grades.stream()
                .mapToDouble(Grade::getScore)
                .sum();

        return sum / grades.size();
    }

    // Get average grade by subject
    public double getAverageGradeBySubject(String subjectName) {
        List<Grade> grades = getGradesBySubject(subjectName);
        if (grades.isEmpty()) return 0.0;

        double sum = grades.stream()
                .mapToDouble(Grade::getScore)
                .sum();

        return sum / grades.size();
    }

    // Get grades sorted by score (highest first)
    public List<Grade> getGradesSortedByScore(boolean descending) {
        lock.readLock().lock();
        try {
            List<Grade> allGrades = new ArrayList<>();
            for (List<Grade> gradeList : gradesByStudentId.values()) {
                allGrades.addAll(gradeList);
            }

            if (descending) {
                allGrades.sort((g1, g2) -> Double.compare(g2.getScore(), g1.getScore()));
            } else {
                allGrades.sort(Comparator.comparingDouble(Grade::getScore));
            }

            return allGrades;
        } finally {
            lock.readLock().unlock();
        }
    }

    // Remove grade by ID
    public boolean removeGrade(String gradeId) {
        if (gradeId == null) return false;

        lock.writeLock().lock();
        try {
            Grade grade = gradesById.remove(gradeId);
            if (grade == null) return false;

            // Remove from date index
            String date = grade.getDate();
            List<Grade> dateGrades = gradesByDate.get(date);
            if (dateGrades != null) {
                dateGrades.remove(grade);
                if (dateGrades.isEmpty()) {
                    gradesByDate.remove(date);
                }
            }

            // Remove from student index
            String studentId = grade.getStudent().getId();
            List<Grade> studentGrades = gradesByStudentId.get(studentId);
            if (studentGrades != null) {
                studentGrades.remove(grade);
                if (studentGrades.isEmpty()) {
                    gradesByStudentId.remove(studentId);
                }
            }

            // Remove from subject index
            String subjectName = grade.getSubject().getName();
            List<Grade> subjectGrades = gradesBySubject.get(subjectName);
            if (subjectGrades != null) {
                subjectGrades.remove(grade);
                if (subjectGrades.isEmpty()) {
                    gradesBySubject.remove(subjectName);
                }
            }

            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Get total number of grades
    public int size() {
        lock.readLock().lock();
        try {
            return gradesById.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    // Check if empty
    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return gradesById.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    // Clear all grades
    public void clear() {
        lock.writeLock().lock();
        try {
            gradesByDate.clear();
            gradesById.clear();
            gradesByStudentId.clear();
            gradesBySubject.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Get statistics
    public Map<String, Object> getStatistics() {
        lock.readLock().lock();
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalGrades", gradesById.size());
            stats.put("uniqueStudents", gradesByStudentId.size());
            stats.put("uniqueSubjects", gradesBySubject.size());
            stats.put("dateRangeSize", gradesByDate.size());

            // Calculate overall average
            double totalScore = 0;
            int count = 0;
            for (Grade grade : gradesById.values()) {
                totalScore += grade.getScore();
                count++;
            }
            stats.put("overallAverage", count > 0 ? totalScore / count : 0.0);

            return stats;
        } finally {
            lock.readLock().unlock();
        }
    }

    // Export to list
    public List<Grade> toList() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(gradesById.values());
        } finally {
            lock.readLock().unlock();
        }
    }
}
