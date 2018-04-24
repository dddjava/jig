package jig.diagram;

import java.util.Arrays;

public enum DiagramType {
    ServiceMethodCallHierarchy,
    PackageDependency;

    public static DiagramType resolve(String diagramType) {
        for (DiagramType type : values()) {
            if (type.name().equalsIgnoreCase(diagramType.toLowerCase())) {
                return type;
            }
        }

        throw new IllegalArgumentException("diagramTypeが未指定もしくは誤っています。次のいずれかを入力してください。: " + Arrays.toString(values()));
    }
}
