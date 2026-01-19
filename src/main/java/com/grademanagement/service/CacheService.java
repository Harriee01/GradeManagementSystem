package com.grademanagement.service;

import com.grademanagement.collections.StudentCache;
import com.grademanagement.model.Student;
import java.util.Map;

public class CacheService {
    private final StudentCache<String, Student> cache;

    public CacheService() {
        this.cache = new StudentCache<>(100, 3600000); // 100 items, 1 hour timeout
    }

    public void put(String key, Student student) {
        cache.put(key, student);
    }

    public Student get(String key) {
        return cache.get(key);
    }

    public void remove(String key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }

    public double getHitRate() {
        return cache.getHitRate();
    }

    public void displayContents() {
        cache.displayContents();
    }

    public void setMaxSize(int newSize) {
        cache.setMaxSize(newSize);
    }
}