package com.grademanagement.concurrent;

import com.grademanagement.model.Student;
import java.nio.file.Path;
import java.util.concurrent.Callable;

public class ReportGenerator implements Callable<Path> {
    private final Student student;
    private final Path outputDir;

    public ReportGenerator(Student student, Path outputDir) {
        this.student = student;
        this.outputDir = outputDir;
    }

    @Override
    public Path call() throws Exception {
        // Generate report logic
        String filename = "report_" + student.getId() + ".txt";
        Path filePath = outputDir.resolve(filename);
        // Write report to file
        return filePath;
    }
}