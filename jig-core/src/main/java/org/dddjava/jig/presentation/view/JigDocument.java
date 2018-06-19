package org.dddjava.jig.presentation.view;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 取り扱うドキュメントの種類
 */
public enum JigDocument {
    ServiceMethodCallHierarchy("service-method-call-hierarchy"),
    PackageDependency("package-dependency"),
    ApplicationList("application"),
    DomainList("domain"),
    BranchList("branches"),
    EnumUsage("enum-usage");

    private final String documentFileName;

    JigDocument(String documentFileName) {
        this.documentFileName = documentFileName;
    }

    public String fileName() {
        return documentFileName;
    }

    public static List<JigDocument> resolve(String diagramTypes) {
        return Arrays.stream(diagramTypes.split(","))
                .map(JigDocument::valueOf)
                .collect(Collectors.toList());
    }
}
