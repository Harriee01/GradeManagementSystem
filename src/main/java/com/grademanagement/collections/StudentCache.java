package com.grademanagement.collections;

import com.grademanagement.model.interfaces.Cacheable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

// Generic LRU (Least Recently Used) cache implementation
public class StudentCache<K, V extends Cacheable> {
    private final int maxSize;                           // Maximum cache size
    private final Map<K, V> cache;                       // Concurrent map for thread safety
    private final LinkedHashMap<K, Long> accessOrder;    // Track access order for LRU
    private final ReentrantLock lock;                    // Lock for synchronization
    private long defaultTimeout;                         // Default expiration time in milliseconds
    private long hits;                                   // Cache hit counter
    private long misses;                                 // Cache miss counter

    public StudentCache(int maxSize, long defaultTimeout) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Cache size must be positive");
        }
        if (defaultTimeout <= 0) {
            throw new IllegalArgumentException("Timeout must be positive");
        }

        this.maxSize = maxSize;
        this.defaultTimeout = defaultTimeout;
        this.cache = new ConcurrentHashMap<>(maxSize);
        this.accessOrder = new LinkedHashMap<>(maxSize, 0.75f, true); // Access-order
        this.lock = new ReentrantLock(true); // Fair lock
        this.hits = 0;
        this.misses = 0;
    }

    // Put value in cache with automatic eviction if full
    public void put(K key, V value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key and value cannot be null");
        }

        lock.lock();
        try {
            // Check if cache is full and evict LRU item
            if (cache.size() >= maxSize && !cache.containsKey(key)) {
                evictLRU();
            }

            cache.put(key, value);
            accessOrder.put(key, System.currentTimeMillis());

            // Clean expired entries periodically
            if (cache.size() % 10 == 0) {  // Clean every 10 inserts
                cleanExpired();
            }
        } finally {
            lock.unlock();
        }
    }

    // Get value from cache, updates access time
    public V get(K key) {
        if (key == null) {
            return null;
        }

        lock.lock();
        try {
            V value = cache.get(key);
            if (value != null) {
                // Update access time
                accessOrder.put(key, System.currentTimeMillis());

                // Check if expired
                if (value.isExpired(defaultTimeout)) {
                    cache.remove(key);
                    accessOrder.remove(key);
                    misses++;
                    return null;
                }
                hits++;
                return value;
            }
            misses++;
            return null;
        } finally {
            lock.unlock();
        }
    }

    // Check if key exists in cache
    public boolean containsKey(K key) {
        if (key == null) return false;

        lock.lock();
        try {
            V value = cache.get(key);
            if (value == null) return false;

            if (value.isExpired(defaultTimeout)) {
                cache.remove(key);
                accessOrder.remove(key);
                return false;
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    // Remove specific key from cache
    public V remove(K key) {
        if (key == null) return null;

        lock.lock();
        try {
            accessOrder.remove(key);
            return cache.remove(key);
        } finally {
            lock.unlock();
        }
    }

    // Evict least recently used item
    private void evictLRU() {
        if (!accessOrder.isEmpty()) {
            K lruKey = accessOrder.entrySet().iterator().next().getKey();
            cache.remove(lruKey);
            accessOrder.remove(lruKey);
        }
    }

    // Clean expired entries
    private void cleanExpired() {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<K, Long>> iterator = accessOrder.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<K, Long> entry = iterator.next();
            V value = cache.get(entry.getKey());
            if (value != null && value.isExpired(defaultTimeout)) {
                cache.remove(entry.getKey());
                iterator.remove();
            }
        }
    }

    // Get all cache keys
    public Set<K> keySet() {
        lock.lock();
        try {
            cleanExpired(); // Clean before returning
            return new HashSet<>(cache.keySet());
        } finally {
            lock.unlock();
        }
    }

    // Get all cache values
    public Collection<V> values() {
        lock.lock();
        try {
            cleanExpired(); // Clean before returning
            return new ArrayList<>(cache.values());
        } finally {
            lock.unlock();
        }
    }

    // Get cache size
    public int size() {
        lock.lock();
        try {
            cleanExpired(); // Clean before returning size
            return cache.size();
        } finally {
            lock.unlock();
        }
    }

    // Check if cache is empty
    public boolean isEmpty() {
        lock.lock();
        try {
            cleanExpired(); // Clean before checking
            return cache.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    // Clear entire cache
    public void clear() {
        lock.lock();
        try {
            cache.clear();
            accessOrder.clear();
            hits = 0;
            misses = 0;
        } finally {
            lock.unlock();
        }
    }

    // Get cache hit rate
    public double getHitRate() {
        lock.lock();
        try {
            long total = hits + misses;
            return total > 0 ? (hits * 100.0) / total : 0.0;
        } finally {
            lock.unlock();
        }
    }

    // Get cache statistics
    public Map<String, Object> getStats() {
        lock.lock();
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("size", cache.size());
            stats.put("maxSize", maxSize);
            stats.put("hits", hits);
            stats.put("misses", misses);
            stats.put("hitRate", getHitRate());
            stats.put("defaultTimeout", defaultTimeout);
            stats.put("loadFactor", (double) cache.size() / maxSize);
            return stats;
        } finally {
            lock.unlock();
        }
    }

    // Set new max size (may trigger eviction)
    public void setMaxSize(int newMaxSize) {
        if (newMaxSize <= 0) {
            throw new IllegalArgumentException("Cache size must be positive");
        }

        lock.lock();
        try {
            // Evict if new size is smaller
            while (cache.size() > newMaxSize) {
                evictLRU();
            }
            // Note: We can't resize ConcurrentHashMap, so we just update the limit
        } finally {
            lock.unlock();
        }
    }

    // Set new timeout
    public void setDefaultTimeout(long timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Timeout must be positive");
        }

        lock.lock();
        try {
            this.defaultTimeout = timeout;
            cleanExpired(); // Clean with new timeout
        } finally {
            lock.unlock();
        }
    }

    // Display cache contents (for debugging)
    public void displayContents() {
        lock.lock();
        try {
            System.out.println("=== CACHE CONTENTS ===");
            System.out.printf("Size: %d/%d (%.1f%% full)\n",
                    cache.size(), maxSize, (cache.size() * 100.0) / maxSize);
            System.out.printf("Hit Rate: %.2f%%\n", getHitRate());
            System.out.println("Entries:");

            for (Map.Entry<K, V> entry : cache.entrySet()) {
                V value = entry.getValue();
                long age = System.currentTimeMillis() - value.getCreationTime();
                boolean expired = value.isExpired(defaultTimeout);

                System.out.printf("  %s -> %s (Age: %dms, Expired: %s)\n",
                        entry.getKey(), value.getCacheKey(), age, expired ? "Yes" : "No");
            }
        } finally {
            lock.unlock();
        }
    }
}
