package com.grademanagement.model.interfaces;

// Interface representing a Student type
public interface StudentType {

    /**
     * Returns the type of student (e.g., "Undergraduate", "Graduate", "PartTime").
     * Each concrete student class must implement this method.
     */
    String getStudentType();

}
