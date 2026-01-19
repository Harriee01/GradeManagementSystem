package com.grademanagement.io;

import java.nio.file.*;
import java.io.IOException;

public class FileWatcher {

    public void watchDirectory(Path dir) throws IOException {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            dir.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    System.out.println("File changed: " + event.context());
                }
                key.reset();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}