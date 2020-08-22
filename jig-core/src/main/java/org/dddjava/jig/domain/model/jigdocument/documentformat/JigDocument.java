package org.dddjava.jig.domain.model.jigdocument.documentformat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 取り扱うドキュメントの種類
 */
public enum JigDocument {

    BusinessRuleList(BusinessRuleDocument.BusinessRuleList, "business-rule"),
    PackageRelationDiagram(BusinessRuleDocument.PackageRelationDiagram, "package-relation"),
    BusinessRuleRelationDiagram(BusinessRuleDocument.BusinessRuleRelationDiagram, "business-rule-relation"),
    OverconcentrationBusinessRuleDiagram(BusinessRuleDocument.OverconcentrationBusinessRuleDiagram, "overconcentration-business-rule"),

    CategoryDiagram(BusinessRuleDocument.CategoryDiagram, "category"),
    CategoryUsageDiagram(BusinessRuleDocument.CategoryUsageDiagram, "category-usage"),

    ApplicationList(ApplicationDocument.ApplicationList, "application"),
    ServiceMethodCallHierarchyDiagram(ApplicationDocument.ServiceMethodCallHierarchyDiagram, "service-method-call-hierarchy"),
    CompositeUsecaseDiagram(ApplicationDocument.CompositeUsecaseDiagram, "composite-usecase"),

    ArchitectureDiagram(ArchitectureDocument.ArchitectureDiagram, "architecture"),
    ;

    private final String documentFileName;

    JigDocument(ApplicationDocument document, String fileName) {
        this(fileName);
    }

    JigDocument(BusinessRuleDocument document, String fileName) {
        this(fileName);
    }

    JigDocument(ArchitectureDocument document, String fileName) {
        this(fileName);
    }

    JigDocument(String documentFileName) {
        this.documentFileName = documentFileName;
    }

    public static List<JigDocument> canonical() {
        return Arrays.stream(values())
                .collect(Collectors.toList());
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
