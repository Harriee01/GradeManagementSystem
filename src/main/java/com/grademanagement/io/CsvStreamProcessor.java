package com.grademanagement.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class CsvStreamProcessor {

    public Stream<String> processLargeFile(Path filePath) throws IOException {
        return Files.lines(filePath)
                .skip(1) // Skip header
                .parallel(); // Process in parallel
    }
}