package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JavaFileParser {

    public record ParsedFile(
            String fullyQualifiedClassName,
            String packageName,
            String simpleClassName,
            List<String> imports
    ) {}

    public ParsedFile parse(Path file) throws IOException {
        String simpleClassName = file.getFileName().toString().replace(".java", "");
        String packageName = "";
        List<String> imports = new ArrayList<>();

        for (String raw : Files.readAllLines(file)) {
            String line = raw.trim();

            if (line.startsWith("package ")) {
                packageName = line.replace("package ", "").replace(";", "").trim();
            } else if (line.startsWith("import ") && !line.startsWith("import static ")) {
                imports.add(line.replace("import ", "").replace(";", "").trim());
            }
        }

        String fqcn = packageName.isEmpty() ? simpleClassName : packageName + "." + simpleClassName;
        return new ParsedFile(fqcn, packageName, simpleClassName, imports);
    }
}
