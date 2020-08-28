package org.dddjava.jig.domain.model.jigdocument.implementation;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.*;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRulePackage;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRulePackages;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelation;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

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
        TypeIdentifiers isolatedTypes = businessRules.isolatedTypes();

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

        for (ClassRelation classRelation : businessRules.internalClassRelations().list()) {
            graph.add(classRelation.dotText());
        }

        return DiagramSource.createDiagramSource(documentName, graph.toString());
    }

    public DiagramSources overconcentrationRelationDotText(JigDocumentContext jigDocumentContext) {
        if (businessRules.empty()) return DiagramSource.empty();
        Map<BusinessRule, TypeIdentifiers> map = businessRules.overconcentrationMap();
        if (map.isEmpty()) return DiagramSource.empty();

        DocumentName documentName = jigDocumentContext.documentName(JigDocument.OverconcentrationBusinessRuleDiagram);
        StringJoiner graph = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("rankdir=LR;")
                //.add("layout=\"circo\";")
                .add("node [shape=box,style=filled,fillcolor=lightgoldenrod];");

        List<TypeIdentifier> targetTypeIdentifiers = map.entrySet().stream()
                .flatMap(entry -> Stream.concat(Stream.of(entry.getKey().typeIdentifier()), entry.getValue().list().stream()))
                .collect(toList());
        for (BusinessRule businessRule : businessRules.list()) {
            if (targetTypeIdentifiers.contains(businessRule.typeIdentifier())) {
                Node node = Node.businessRuleNodeOf(businessRule);
                if (map.containsKey(businessRule)) {
                    node.big();
                } else {
                    node.weakColor();
                }
                graph.add(node.asText());
            }
        }

        for (Map.Entry<BusinessRule, TypeIdentifiers> entry : map.entrySet()) {
            for (TypeIdentifier fromTypeIdentifier : entry.getValue().list()) {
                graph.add(new ClassRelation(fromTypeIdentifier, entry.getKey().typeIdentifier()).dotText());
            }
        }

        return DiagramSource.createDiagramSource(documentName, graph.toString());
    }
}
