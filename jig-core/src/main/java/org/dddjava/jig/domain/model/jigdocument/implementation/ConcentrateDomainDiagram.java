package org.dddjava.jig.domain.model.jigdocument.implementation;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.*;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.parts.relation.class_.ClassRelation;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ConcentrateDomainDiagram implements DiagramSourceWriter {

    BusinessRules businessRules;

    public ConcentrateDomainDiagram(BusinessRules businessRules) {
        this.businessRules = businessRules;
    }

    public DiagramSources sources(JigDocumentContext jigDocumentContext) {
        if (businessRules.empty()) return DiagramSource.empty();
        Map<BusinessRule, TypeIdentifiers> map = businessRules.overconcentrationMap();
        if (map.isEmpty()) return DiagramSource.empty();

        DocumentName documentName = DocumentName.of(JigDocument.OverconcentrationBusinessRuleDiagram);
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
