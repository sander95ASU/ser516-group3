package org.example;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

public class FileSystemNavigator {

    public List<Path> findJavaFiles(Path rootDir) throws IOException {
        if (!Files.isDirectory(rootDir)) {
            throw new IllegalArgumentException("Not a directory: " + rootDir);
        }

        return Files.walk(rootDir)
                .filter(p -> p.toString().endsWith(".java"))
                .collect(Collectors.toList());
    }
}
