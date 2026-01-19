package com.grademanagement.repository;

import com.grademanagement.model.AuditLog;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

// Thread-safe audit log repository
public class AuditRepository {
    // Primary storage: Audit ID -> AuditLog
    private final Map<String, AuditLog> logsById;

    // Indexes for fast lookups
    private final SortedMap<Long, List<AuditLog>> logsByTimestamp; // Timestamp -> Logs
    private final Map<String, List<AuditLog>> logsByOperation;     // Operation -> Logs
    private final Map<String, List<AuditLog>> logsByUserId;       // User ID -> Logs
    private final Map<String, List<AuditLog>> logsByEntityType;   // Entity Type -> Logs

    // Maximum number of logs to keep (to prevent memory issues)
    private final int maxLogs;
    private static final int DEFAULT_MAX_LOGS = 10000;

    // Statistics
    private long totalLogs;
    private long successfulOperations;
    private long failedOperations;

    public AuditRepository() {
        this(DEFAULT_MAX_LOGS);
    }

    public AuditRepository(int maxLogs) {
        if (maxLogs <= 0) {
            throw new IllegalArgumentException("Max logs must be positive");
        }

        this.maxLogs = maxLogs;
        this.logsById = new ConcurrentHashMap<>();
        this.logsByTimestamp = new ConcurrentSkipListMap<>(Collections.reverseOrder()); // Newest first
        this.logsByOperation = new ConcurrentHashMap<>();
        this.logsByUserId = new ConcurrentHashMap<>();
        this.logsByEntityType = new ConcurrentHashMap<>();
        this.totalLogs = 0;
        this.successfulOperations = 0;
        this.failedOperations = 0;
    }

    // Add an audit log entry
    public void addLog(AuditLog log) {
        if (log == null) {
            throw new IllegalArgumentException("Audit log cannot be null");
        }

        // Check if we need to clean old logs
        if (totalLogs >= maxLogs) {
            cleanOldLogs();
        }

        String logId = log.getId();

        // Store in primary map
        logsById.put(logId, log);

        // Update indexes
        updateIndexes(logId, log);

        // Update statistics
        totalLogs++;
        if (log.isSuccess()) {
            successfulOperations++;
        } else {
            failedOperations++;
        }
    }

    // Update indexes for a log
    private void updateIndexes(String logId, AuditLog log) {
        // Index by timestamp (using epoch milliseconds)
        long timestamp = log.getTimestamp().atZone(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli();

        logsByTimestamp
                .computeIfAbsent(timestamp, k -> new ArrayList<>())
                .add(log);

        // Index by operation
        logsByOperation
                .computeIfAbsent(log.getOperation(), k -> new ArrayList<>())
                .add(log);

        // Index by user ID
        logsByUserId
                .computeIfAbsent(log.getUserId(), k -> new ArrayList<>())
                .add(log);

        // Index by entity type
        logsByEntityType
                .computeIfAbsent(log.getEntityType(), k -> new ArrayList<>())
                .add(log);
    }

    // Clean old logs to maintain size limit
    private void cleanOldLogs() {
        int logsToRemove = (int) (maxLogs * 0.1); // Remove 10% of logs

        // Get oldest logs
        List<Long> timestamps = new ArrayList<>(logsByTimestamp.keySet());
        Collections.sort(timestamps); // Sort ascending (oldest first)

        int removed = 0;
        for (Long timestamp : timestamps) {
            if (removed >= logsToRemove) break;

            List<AuditLog> logs = logsByTimestamp.get(timestamp);
            if (logs != null) {
                for (AuditLog log : logs) {
                    if (removed >= logsToRemove) break;

                    removeLog(log.getId());
                    removed++;
                }
            }
        }
    }

    // Remove a log
    private void removeLog(String logId) {
        AuditLog log = logsById.remove(logId);
        if (log == null) return;

        // Remove from timestamp index
        long timestamp = log.getTimestamp().atZone(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli();
        List<AuditLog> timestampLogs = logsByTimestamp.get(timestamp);
        if (timestampLogs != null) {
            timestampLogs.remove(log);
            if (timestampLogs.isEmpty()) {
                logsByTimestamp.remove(timestamp);
            }
        }

        // Remove from operation index
        List<AuditLog> operationLogs = logsByOperation.get(log.getOperation());
        if (operationLogs != null) {
            operationLogs.remove(log);
            if (operationLogs.isEmpty()) {
                logsByOperation.remove(log.getOperation());
            }
        }

        // Remove from user index
        List<AuditLog> userLogs = logsByUserId.get(log.getUserId());
        if (userLogs != null) {
            userLogs.remove(log);
            if (userLogs.isEmpty()) {
                logsByUserId.remove(log.getUserId());
            }
        }

        // Remove from entity type index
        List<AuditLog> entityLogs = logsByEntityType.get(log.getEntityType());
        if (entityLogs != null) {
            entityLogs.remove(log);
            if (entityLogs.isEmpty()) {
                logsByEntityType.remove(log.getEntityType());
            }
        }

        // Update statistics
        totalLogs--;
        if (log.isSuccess()) {
            successfulOperations--;
        } else {
            failedOperations--;
        }
    }

    // Get log by ID
    public AuditLog getLog(String logId) {
        return logsById.get(logId);
    }

    // Get all logs
    public List<AuditLog> getAllLogs() {
        return new ArrayList<>(logsById.values());
    }

    // Get logs by timestamp range
    public List<AuditLog> getLogsByTimeRange(Date from, Date to) {
        long fromMillis = from.getTime();
        long toMillis = to.getTime();

        List<AuditLog> result = new ArrayList<>();
        SortedMap<Long, List<AuditLog>> subMap = logsByTimestamp.subMap(toMillis, fromMillis);

        for (List<AuditLog> logs : subMap.values()) {
            result.addAll(logs);
        }

        return result;
    }

    // Get latest N logs
    public List<AuditLog> getLatestLogs(int n) {
        List<AuditLog> result = new ArrayList<>();
        int count = 0;

        for (List<AuditLog> logs : logsByTimestamp.values()) {
            for (AuditLog log : logs) {
                if (count >= n) break;
                result.add(log);
                count++;
            }
            if (count >= n) break;
        }

        return result;
    }

    // Get logs by operation
    public List<AuditLog> getLogsByOperation(String operation) {
        List<AuditLog> logs = logsByOperation.get(operation);
        return logs != null ? new ArrayList<>(logs) : new ArrayList<>();
    }

    // Get logs by user ID
    public List<AuditLog> getLogsByUserId(String userId) {
        List<AuditLog> logs = logsByUserId.get(userId);
        return logs != null ? new ArrayList<>(logs) : new ArrayList<>();
    }

    // Get logs by entity type
    public List<AuditLog> getLogsByEntityType(String entityType) {
        List<AuditLog> logs = logsByEntityType.get(entityType);
        return logs != null ? new ArrayList<>(logs) : new ArrayList<>();
    }

    // Search logs with multiple criteria
    public List<AuditLog> searchLogs(String operation, String userId,
                                     String entityType, String entityId,
                                     Boolean success, Date fromDate, Date toDate) {
        List<AuditLog> candidates = getAllLogs();

        // Apply filters
        if (operation != null && !operation.trim().isEmpty()) {
            candidates = candidates.stream()
                    .filter(log -> operation.equals(log.getOperation()))
                    .collect(Collectors.toList());
        }

        if (userId != null && !userId.trim().isEmpty()) {
            candidates = candidates.stream()
                    .filter(log -> userId.equals(log.getUserId()))
                    .collect(Collectors.toList());
        }

        if (entityType != null && !entityType.trim().isEmpty()) {
            candidates = candidates.stream()
                    .filter(log -> entityType.equals(log.getEntityType()))
                    .collect(Collectors.toList());
        }

        if (entityId != null && !entityId.trim().isEmpty()) {
            candidates = candidates.stream()
                    .filter(log -> entityId.equals(log.getEntityId()))
                    .collect(Collectors.toList());
        }

        if (success != null) {
            candidates = candidates.stream()
                    .filter(log -> log.isSuccess() == success)
                    .collect(Collectors.toList());
        }

        if (fromDate != null) {
            long fromMillis = fromDate.getTime();
            candidates = candidates.stream()
                    .filter(log -> {
                        long logMillis = log.getTimestamp().atZone(java.time.ZoneId.systemDefault())
                                .toInstant().toEpochMilli();
                        return logMillis >= fromMillis;
                    })
                    .collect(Collectors.toList());
        }

        if (toDate != null) {
            long toMillis = toDate.getTime();
            candidates = candidates.stream()
                    .filter(log -> {
                        long logMillis = log.getTimestamp().atZone(java.time.ZoneId.systemDefault())
                                .toInstant().toEpochMilli();
                        return logMillis <= toMillis;
                    })
                    .collect(Collectors.toList());
        }

        return candidates;
    }

    // Get statistics
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalLogs", totalLogs);
        stats.put("successfulOperations", successfulOperations);
        stats.put("failedOperations", failedOperations);
        stats.put("successRate", totalLogs > 0 ? (successfulOperations * 100.0) / totalLogs : 0.0);

        // Operation distribution
        Map<String, Integer> operationDistribution = new HashMap<>();
        for (Map.Entry<String, List<AuditLog>> entry : logsByOperation.entrySet()) {
            operationDistribution.put(entry.getKey(), entry.getValue().size());
        }
        stats.put("operationDistribution", operationDistribution);

        // User activity
        Map<String, Integer> userActivity = new HashMap<>();
        for (Map.Entry<String, List<AuditLog>> entry : logsByUserId.entrySet()) {
            userActivity.put(entry.getKey(), entry.getValue().size());
        }
        stats.put("userActivity", userActivity);

        // Entity type distribution
        Map<String, Integer> entityDistribution = new HashMap<>();
        for (Map.Entry<String, List<AuditLog>> entry : logsByEntityType.entrySet()) {
            entityDistribution.put(entry.getKey(), entry.getValue().size());
        }
        stats.put("entityDistribution", entityDistribution);

        // Time range
        if (!logsByTimestamp.isEmpty()) {
            Long oldestTimestamp = logsByTimestamp.lastKey();
            Long newestTimestamp = logsByTimestamp.firstKey();

            Date oldestDate = new Date(oldestTimestamp);
            Date newestDate = new Date(newestTimestamp);

            stats.put("oldestLog", oldestDate);
            stats.put("newestLog", newestDate);

            long days = (newestTimestamp - oldestTimestamp) / (1000 * 60 * 60 * 24);
            stats.put("timeSpanDays", days > 0 ? days : 1);
        }

        return stats;
    }

    // Export logs to CSV
    public String exportToCSV() {
        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("ID,Timestamp,Operation,UserID,EntityType,EntityID,Details,Success\n");

        // Data (newest first)
        for (List<AuditLog> logs : logsByTimestamp.values()) {
            for (AuditLog log : logs) {
                csv.append(log.toCSV()).append("\n");
            }
        }

        return csv.toString();
    }

    // Import logs from CSV
    public int importFromCSV(String csvData) {
        if (csvData == null || csvData.trim().isEmpty()) {
            return 0;
        }

        String[] lines = csvData.split("\n");
        int imported = 0;

        // Skip header
        for (int i = 1; i < lines.length; i++) {
            try {
                AuditLog log = AuditLog.fromCSV(lines[i]);
                addLog(log);
                imported++;
            } catch (Exception e) {
                System.err.println("Failed to import line " + i + ": " + e.getMessage());
            }
        }

        return imported;
    }

    // Clear all logs
    public void clear() {
        logsById.clear();
        logsByTimestamp.clear();
        logsByOperation.clear();
        logsByUserId.clear();
        logsByEntityType.clear();
        totalLogs = 0;
        successfulOperations = 0;
        failedOperations = 0;
    }

    // Get size
    public int size() {
        return logsById.size();
    }

    // Check if empty
    public boolean isEmpty() {
        return logsById.isEmpty();
    }

    // Get maximum logs capacity
    public int getMaxLogs() {
        return maxLogs;
    }

    // Set maximum logs (with cleanup if needed)
    public void setMaxLogs(int newMax) {
        if (newMax <= 0) {
            throw new IllegalArgumentException("Max logs must be positive");
        }

        // Clean old logs if new max is smaller
        while (totalLogs > newMax) {
            cleanOldLogs();
        }
    }

    // Get formatted statistics report
    public String getStatisticsReport() {
        Map<String, Object> stats = getStatistics();
        StringBuilder report = new StringBuilder();

        report.append("=== AUDIT LOG STATISTICS ===\n");
        report.append(String.format("Total Logs: %d\n", stats.get("totalLogs")));
        report.append(String.format("Successful Operations: %d\n", stats.get("successfulOperations")));
        report.append(String.format("Failed Operations: %d\n", stats.get("failedOperations")));
        report.append(String.format("Success Rate: %.2f%%\n", stats.get("successRate")));

        if (stats.containsKey("oldestLog") && stats.containsKey("newestLog")) {
            report.append(String.format("Time Span: %s to %s\n",
                    stats.get("oldestLog"), stats.get("newestLog")));
        }

        report.append("\nOperation Distribution:\n");
        Map<String, Integer> opDist = (Map<String, Integer>) stats.get("operationDistribution");
        for (Map.Entry<String, Integer> entry : opDist.entrySet()) {
            report.append(String.format("  - %s: %d\n", entry.getKey(), entry.getValue()));
        }

        return report.toString();
    }
}
