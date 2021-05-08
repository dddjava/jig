package org.dddjava.jig.domain.model.documents.implementation;

import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRulePackage;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRulePackages;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.parts.relation.class_.ClassRelation;

import java.util.StringJoiner;

/**
 * ビジネスルールの関連
 */
public class BusinessRuleRelationDiagram implements DiagramSourceWriter {

    BusinessRules businessRules;

    public BusinessRuleRelationDiagram(BusinessRules businessRules) {
        this.businessRules = businessRules;
    }

    public DiagramSources sources(JigDocumentContext jigDocumentContext) {
        return sources(jigDocumentContext, businessRules, DocumentName.of(JigDocument.BusinessRuleRelationDiagram));
    }

    DiagramSources sources(JigDocumentContext jigDocumentContext, BusinessRules targetBusinessRules, DocumentName documentName) {
        if (targetBusinessRules.empty()) {
            return DiagramSource.empty();
        }

        StringJoiner graph = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("newrank=true;")
                .add("node [shape=box,style=filled,fillcolor=lightgoldenrod];");

        TypeIdentifiers isolatedTypes = targetBusinessRules.isolatedTypes();
        BusinessRulePackages businessRulePackages = targetBusinessRules.businessRulePackages();
        for (BusinessRulePackage businessRulePackage : businessRulePackages.list()) {
            PackageIdentifier packageIdentifier = businessRulePackage.packageIdentifier();

            Subgraph subgraph = new Subgraph(packageIdentifier.asText())
                    .label(jigDocumentContext.packageIdentifierFormatter().format(packageIdentifier))
                    .fillColor("lemonchiffon").color("lightgoldenrod").borderWidth(2);

            BusinessRules businessRules = businessRulePackage.businessRules();
            for (BusinessRule businessRule : businessRules.list()) {
                Node node = Node.businessRuleNodeOf(businessRule);
                if (isolatedTypes.contains(businessRule.typeIdentifier())) {
                    node.warning();
                }
                subgraph.add(node.asText());
            }

            graph.add(subgraph.toString());
        }

        for (ClassRelation classRelation : targetBusinessRules.internalClassRelations().list()) {
            graph.add(classRelation.dotText());
        }

        return DiagramSource.createDiagramSource(documentName, graph.toString());
    }
}
