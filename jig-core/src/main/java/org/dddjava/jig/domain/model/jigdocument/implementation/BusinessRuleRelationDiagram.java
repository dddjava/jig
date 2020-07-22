package org.dddjava.jig.domain.model.jigdocument.implementation;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.*;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRulePackage;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRulePackages;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelation;

import java.util.List;
import java.util.StringJoiner;

/**
 * ビジネスルールの関連
 */
public class BusinessRuleRelationDiagram {

    BusinessRules businessRules;

    public BusinessRuleRelationDiagram(BusinessRules businessRules) {
        this.businessRules = businessRules;
    }

    public DiagramSources relationDotText(JigDocumentContext jigDocumentContext, PackageIdentifierFormatter packageIdentifierFormatter) {

        if (businessRules.empty()) {
            return DiagramSource.empty();
        }

        DocumentName documentName = jigDocumentContext.documentName(JigDocument.BusinessRuleRelationDiagram);
        StringJoiner graph = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("node [shape=box,style=filled,fillcolor=lightgoldenrod];");

        BusinessRulePackages businessRulePackages = businessRules.businessRulePackages();
        for (BusinessRulePackage businessRulePackage : businessRulePackages.list()) {
            PackageIdentifier packageIdentifier = businessRulePackage.packageIdentifier();

            Subgraph subgraph = new Subgraph(packageIdentifier.asText())
                    .label(packageIdentifier.format(packageIdentifierFormatter))
                    .fillColor("lemonchiffon").color("lightgoldenrod").borderWidth(2);

            List<BusinessRule> businessRules = businessRulePackage.businessRules().list();
            for (BusinessRule businessRule : businessRules) {
                TypeAlias typeAlias = jigDocumentContext.aliasFinder().find(businessRule.type().identifier());

                Node node = Node.controllerNodeOf(businessRule.type().identifier())
                        .label(typeAlias.nodeLabel())
                        .highlightColorIf(typeAlias.markedCore());

                subgraph.add(node.asText());
            }

            graph.add(subgraph.toString());
        }

        for (ClassRelation classRelation : businessRules.internalClassRelations().list()) {
            graph.add(classRelation.dotText());
        }

        return DiagramSource.createDiagramSource(documentName, graph.toString());
    }
}
