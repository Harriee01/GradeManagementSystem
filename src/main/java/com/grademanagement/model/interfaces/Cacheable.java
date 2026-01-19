package com.grademanagement.model.interfaces;

// Interface for cacheable objects with expiration
public interface Cacheable {
    String getCacheKey();           // Unique key for caching
    long getCreationTime();         // Creation timestamp
    boolean isExpired(long timeout); // Check if expired
}
