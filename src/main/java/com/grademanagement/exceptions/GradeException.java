package com.grademanagement.exceptions;

// Custom exception for grade-related errors
public class GradeException extends Exception {
    public GradeException(String message) {
        super(message);  // Call parent constructor with error message
    }

    public GradeException(String message, Throwable cause) {
        super(message, cause);  // Include underlying cause for better debugging
    }
}
