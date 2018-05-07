package org.dddjava.jig.domain.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum DocumentType {
    ServiceMethodCallHierarchy,
    PackageDependency,
    ApplicationList,
    DomainList,
    EnumUsage;

    public static List<DocumentType> resolve(String diagramTypes) {
        return Arrays.stream(diagramTypes.split(","))
                .map(DocumentType::valueOf)
                .collect(Collectors.toList());
    }
}
