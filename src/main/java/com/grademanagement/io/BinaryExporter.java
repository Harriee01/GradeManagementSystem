package com.grademanagement.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class BinaryExporter {

    public void exportToBinary(Object data, Path outputPath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                Files.newOutputStream(outputPath))) {
            oos.writeObject(data);
        }
    }
}