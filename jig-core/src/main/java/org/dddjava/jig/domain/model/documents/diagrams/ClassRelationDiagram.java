package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifiers;
import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.information.module.JigTypesPackage;
import org.dddjava.jig.domain.model.information.relation.classes.ClassRelation;
import org.dddjava.jig.domain.model.information.relation.classes.ClassRelations;
import org.dddjava.jig.domain.model.information.type.JigType;
import org.dddjava.jig.domain.model.information.type.JigTypes;

import java.util.StringJoiner;

/**
 * JigTypeの関連図
 */
public class ClassRelationDiagram implements DiagramSourceWriter {

    JigTypes jigTypes;

    public ClassRelationDiagram(JigTypes jigTypes) {
        this.jigTypes = jigTypes;
    }

    public DiagramSources sources() {
        return sources(jigTypes, DocumentName.of(JigDocument.BusinessRuleRelationDiagram));
    }

    DiagramSources sources(JigTypes jigTypes, DocumentName documentName) {
        if (jigTypes.empty()) {
            return DiagramSource.empty();
        }

        StringJoiner graph = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("newrank=true;")
                .add(Node.DEFAULT);

        // 出力対象の内部だけの関連
        var internalClassRelations = ClassRelations.internalRelation(jigTypes);

        // 関連のないものだけ抽出する
        TypeIdentifiers isolatedTypes = jigTypes
                .filter(jigType -> ClassRelations.internalTypeRelationsFrom(jigTypes, jigType).isEmpty() && ClassRelations.internalTypeRelationsTo(jigTypes, jigType).isEmpty())
                .typeIdentifiers();

        for (JigTypesPackage jigTypesPackage : JigTypesPackage.from(jigTypes)) {
            PackageIdentifier packageIdentifier = jigTypesPackage.packageIdentifier();

            String fqn = packageIdentifier.asText();
            Subgraph subgraph = new Subgraph(fqn)
                    .label(fqn)
                    .fillColor("lemonchiffon").color("lightgoldenrod").borderWidth(2);

            for (JigType jigType : jigTypesPackage.jigTypes()) {
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

        for (ClassRelation classRelation : internalClassRelations.list()) {
            graph.add(classRelation.dotText());
        }

        return DiagramSource.createDiagramSource(documentName, graph.toString());
    }
}
