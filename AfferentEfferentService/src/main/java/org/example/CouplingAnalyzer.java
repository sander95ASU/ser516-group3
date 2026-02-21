package org.example;

import java.util.*;

public class CouplingAnalyzer {

    public record CouplingMetrics(
            String fullyQualifiedClassName,
            String packageName,
            int afferent,
            int efferent,
            double instability
    ) {}

    public List<CouplingMetrics> analyze(List<JavaFileParser.ParsedFile> parsedFiles) {
        Set<String> knownClasses = new HashSet<>();
        for (JavaFileParser.ParsedFile pf : parsedFiles) {
            knownClasses.add(pf.fullyQualifiedClassName());
        }

        // efferent: only count imports that are part of this codebase
        Map<String, Set<String>> efferentMap = new HashMap<>();
        for (JavaFileParser.ParsedFile pf : parsedFiles) {
            Set<String> deps = new HashSet<>();
            for (String imp : pf.imports()) {
                if (knownClasses.contains(imp)) {
                    deps.add(imp);
                }
            }
            efferentMap.put(pf.fullyQualifiedClassName(), deps);
        }

        // afferent: inverse of efferent
        Map<String, Set<String>> afferentMap = new HashMap<>();
        for (String fqcn : knownClasses) {
            afferentMap.put(fqcn, new HashSet<>());
        }
        for (Map.Entry<String, Set<String>> entry : efferentMap.entrySet()) {
            for (String dep : entry.getValue()) {
                afferentMap.get(dep).add(entry.getKey());
            }
        }

        List<CouplingMetrics> results = new ArrayList<>();
        for (JavaFileParser.ParsedFile pf : parsedFiles) {
            String fqcn = pf.fullyQualifiedClassName();
            int ce = efferentMap.get(fqcn).size();
            int ca = afferentMap.get(fqcn).size();
            double instability = (ca + ce == 0) ? 0.0 : (double) ce / (ca + ce);
            results.add(new CouplingMetrics(fqcn, pf.packageName(), ca, ce, instability));
        }

        results.sort((a, b) -> a.fullyQualifiedClassName().compareTo(b.fullyQualifiedClassName()));
        return results;
    }
}
