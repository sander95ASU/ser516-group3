package org.example;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("usage: Main <source-root>");
            System.exit(1);
        }

        Path root = Paths.get(args[0]);
        FileSystemNavigator navigator = new FileSystemNavigator();
        JavaFileParser parser = new JavaFileParser();
        CouplingAnalyzer analyzer = new CouplingAnalyzer();

        List<Path> javaFiles;
        try {
            javaFiles = navigator.findJavaFiles(root);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }

        if (javaFiles.isEmpty()) {
            System.out.println("no .java files found under " + root);
            return;
        }

        List<JavaFileParser.ParsedFile> parsed = new ArrayList<>();
        for (Path file : javaFiles) {
            try {
                parsed.add(parser.parse(file));
            } catch (IOException e) {
                System.err.println("could not parse " + file + ": " + e.getMessage());
            }
        }

        printResults(analyzer.analyze(parsed));
    }

    private static void printResults(List<CouplingAnalyzer.CouplingMetrics> metrics) {
        System.out.printf("%-60s %4s %4s %10s%n", "Class", "Ca", "Ce", "Instability");
        System.out.println("-".repeat(82));
        for (CouplingAnalyzer.CouplingMetrics m : metrics) {
            System.out.printf("%-60s %4d %4d %10.3f%n",
                    m.fullyQualifiedClassName(), m.afferent(), m.efferent(), m.instability());
        }
    }
}
