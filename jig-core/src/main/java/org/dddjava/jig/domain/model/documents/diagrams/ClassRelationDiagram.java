package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.application.JigTypesWithRelationships;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifiers;
import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.information.module.JigPackageWithJigTypes;
import org.dddjava.jig.domain.model.information.relation.graph.Edge;
import org.dddjava.jig.domain.model.information.relation.graph.Edges;
import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.StringJoiner;

/**
 * JigTypeの関連図
 */
public class ClassRelationDiagram implements DiagramSourceWriter {

    JigTypesWithRelationships jigTypesWithRelationships;

    public ClassRelationDiagram(JigTypesWithRelationships jigTypesWithRelationships) {
        this.jigTypesWithRelationships = jigTypesWithRelationships;
    }

    @Override
    public DiagramSources sources(JigDocumentContext jigDocumentContext) {
        return sources(jigDocumentContext.diagramOption(), DocumentName.of(JigDocument.BusinessRuleRelationDiagram));
    }

    DiagramSources sources(JigDiagramOption jigDiagramOption, DocumentName documentName) {
        if (jigTypesWithRelationships.jigTypes().empty()) {
            return DiagramSource.empty();
        }

        StringJoiner graph = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("newrank=true;")
                .add(Node.DEFAULT);

        // 出力対象の内部だけの関連
        var internalClassRelations = jigTypesWithRelationships.typeRelationships();

        // 関連のないものだけ抽出する
        TypeIdentifiers isolatedTypes = jigTypesWithRelationships.jigTypes()
                .filter(jigType -> internalClassRelations.filterFrom(jigType.id()).isEmpty() && internalClassRelations.filterTo(jigType.id()).isEmpty())
                .typeIdentifiers();

        for (JigPackageWithJigTypes jigPackageWithJigTypes : JigPackageWithJigTypes.from(jigTypesWithRelationships.jigTypes())) {
            PackageId packageId = jigPackageWithJigTypes.packageId();

            String fqn = packageId.asText();
            Subgraph subgraph = new Subgraph(fqn)
                    .label(fqn)
                    .fillColor("lemonchiffon").color("lightgoldenrod").borderWidth(2);

            for (JigType jigType : jigPackageWithJigTypes.jigTypes()) {
                Node node = Nodes.businessRuleNodeOf(jigType);
                if (isolatedTypes.contains(jigType.id())) {
                    node.warning();
                }
                if (jigType.isDeprecated()) {
                    node.deprecated();
                }
                subgraph.add(node.asText());
            }

            graph.add(subgraph.toString());
        }

        Edges<TypeIdentifier> edges = jigDiagramOption.transitiveReduction()
                ? internalClassRelations.toEdges().transitiveReduction()
                : internalClassRelations.toEdges();
        for (Edge<TypeIdentifier> edge : edges.list()) {
            graph.add("\"%s\" -> \"%s\";".formatted(edge.from().fullQualifiedName(), edge.to().fullQualifiedName()));
        }

        return DiagramSource.createDiagramSource(documentName, graph.toString());
    }
}
