package com.grademanagement.model.interfaces;

// Generic interface for identifiable entities with type parameter T for ID type
public interface Identifiable<T> {
    T getId();          // Returns ID of type T (String, Integer, etc.)
    void setId(T id);   // Sets ID of type T
}