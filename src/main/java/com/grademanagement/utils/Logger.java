package com.grademanagement.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class Logger {
    private static Logger instance;
    private static final String LOG_FILE = "logs/application.log";

    private Logger() {
        ensureLogDirectory();
    }

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public void log(String message) {
        String logMessage = String.format("[INFO] %s: %s",
                LocalDateTime.now(), message);
        System.out.println(logMessage);
        writeToFile(logMessage);
    }

    public void error(String message) {
        String logMessage = String.format("[ERROR] %s: %s",
                LocalDateTime.now(), message);
        System.err.println(logMessage);
        writeToFile(logMessage);
    }

    private void writeToFile(String message) {
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            writer.write(message + "\n");
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    private void ensureLogDirectory() {
        java.io.File dir = new java.io.File("logs");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}