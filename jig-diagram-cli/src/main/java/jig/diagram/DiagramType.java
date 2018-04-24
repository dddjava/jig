package jig.diagram;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum DiagramType {
    ServiceMethodCallHierarchy,
    PackageDependency;

    public static List<DiagramType> resolve(String diagramTypes) {
        return Arrays.stream(diagramTypes.split(","))
                .map(DiagramType::valueOf)
                .collect(Collectors.toList());
    }
}
