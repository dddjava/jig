package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.classes.type.ClassRelation;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.information.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.information.jigobject.package_.PackageJigTypes;

import java.util.StringJoiner;

/**
 * ビジネスルールの関連
 */
public class ClassRelationDiagram implements DiagramSourceWriter {

    BusinessRules businessRules;

    public ClassRelationDiagram(BusinessRules businessRules) {
        this.businessRules = businessRules;
    }

    public DiagramSources sources(JigDocumentContext jigDocumentContext) {
        return sources(businessRules, DocumentName.of(JigDocument.BusinessRuleRelationDiagram));
    }

    DiagramSources sources(BusinessRules targetBusinessRules, DocumentName documentName) {
        if (targetBusinessRules.empty()) {
            return DiagramSource.empty();
        }

        StringJoiner graph = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("newrank=true;")
                .add(Node.DEFAULT);

        TypeIdentifiers isolatedTypes = targetBusinessRules.isolatedTypes();
        for (PackageJigTypes packageJigTypes : targetBusinessRules.listPackages()) {
            PackageIdentifier packageIdentifier = packageJigTypes.packageIdentifier();

            String fqn = packageIdentifier.asText();
            Subgraph subgraph = new Subgraph(fqn)
                    .label(fqn)
                    .fillColor("lemonchiffon").color("lightgoldenrod").borderWidth(2);

            for (JigType jigType : packageJigTypes.jigTypes()) {
                Node node = Nodes.businessRuleNodeOf(jigType);
                if (isolatedTypes.contains(jigType.typeIdentifier())) {
                    node.warning();
                }
                if (jigType.isDeprecated()) {
                    node.deprecated();
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
