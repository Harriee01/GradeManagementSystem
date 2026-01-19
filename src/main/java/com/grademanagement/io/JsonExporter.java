package com.grademanagement.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonExporter {
    private final Gson gson;

    public JsonExporter() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void exportToJson(Object data, Path outputPath) throws IOException {
        String json = gson.toJson(data);
        Files.writeString(outputPath, json);
    }
}