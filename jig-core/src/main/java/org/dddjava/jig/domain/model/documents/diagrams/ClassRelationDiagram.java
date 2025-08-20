package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.application.CoreTypesAndRelations;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.data.types.TypeIds;
import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.information.relation.graph.Edge;
import org.dddjava.jig.domain.model.information.relation.graph.Edges;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.knowledge.module.JigPackageWithJigTypes;

import java.util.StringJoiner;
import java.util.function.Consumer;

/**
 * JigTypeの関連図
 */
public class ClassRelationDiagram implements DiagramSourceWriter {

    CoreTypesAndRelations coreTypesAndRelations;

    public ClassRelationDiagram(CoreTypesAndRelations coreTypesAndRelations) {
        this.coreTypesAndRelations = coreTypesAndRelations;
    }

    @Override
    public int write(JigDiagramOption jigDiagramOption, Consumer<DiagramSource> diagramSourceWriteProcess) {
        var documentName = DocumentName.of(JigDocument.BusinessRuleRelationDiagram);
        if (coreTypesAndRelations.coreJigTypes().empty()) {
            return 0;
        }

        StringJoiner graph = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("newrank=true;")
                .add(Node.DEFAULT);

        // 出力対象の内部だけの関連
        var internalClassRelations = coreTypesAndRelations.internalTypeRelationships();

        // 関連のないものだけ抽出する
        TypeIds isolatedTypes = coreTypesAndRelations.coreJigTypes()
                .filter(jigType -> internalClassRelations.filterFrom(jigType.id()).isEmpty() && internalClassRelations.filterTo(jigType.id()).isEmpty())
                .typeIds();

        for (JigPackageWithJigTypes jigPackageWithJigTypes : JigPackageWithJigTypes.from(coreTypesAndRelations.coreJigTypes())) {
            PackageId packageId = jigPackageWithJigTypes.packageId();

            String fqn = packageId.asText();
            Subgraph subgraph = new Subgraph(fqn)
                    .label(fqn)
                    .fillColor("lemonchiffon").color("lightgoldenrod").borderWidth(2);

            for (JigType jigType : jigPackageWithJigTypes.jigTypes()) {
                Node node = Node.businessRuleNodeOf(jigType);
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

        Edges<TypeId> edges = jigDiagramOption.transitiveReduction()
                ? internalClassRelations.toEdges().transitiveReduction()
                : internalClassRelations.toEdges();
        for (Edge<TypeId> edge : edges.list()) {
            graph.add("\"%s\" -> \"%s\";".formatted(edge.from().fqn(), edge.to().fqn()));
        }

        diagramSourceWriteProcess.accept(DiagramSource.createDiagramSourceUnit(documentName, graph.toString()));
        return 1;
    }
}
