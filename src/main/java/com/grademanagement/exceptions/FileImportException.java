package com.grademanagement.exceptions;

// Custom exception for file import errors
public class FileImportException extends Exception {
    public FileImportException(String message) {
        super(message);
    }

    public FileImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
