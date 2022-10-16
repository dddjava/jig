package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.parts.classes.type.ClassRelation;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifiers;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ClassRelationConcentrateDiagram implements DiagramSourceWriter {

    BusinessRules businessRules;

    public ClassRelationConcentrateDiagram(BusinessRules businessRules) {
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
                .add(Node.DEFAULT);

        List<TypeIdentifier> targetTypeIdentifiers = map.entrySet().stream()
                .flatMap(entry -> Stream.concat(Stream.of(entry.getKey().typeIdentifier()), entry.getValue().list().stream()))
                .collect(toList());
        for (BusinessRule businessRule : businessRules.list()) {
            if (targetTypeIdentifiers.contains(businessRule.typeIdentifier())) {
                Node node = Nodes.businessRuleNodeOf(businessRule);
                if (map.containsKey(businessRule)) {
                    node.big();
                } else {
                    node.as(NodeRole.準主役);
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
