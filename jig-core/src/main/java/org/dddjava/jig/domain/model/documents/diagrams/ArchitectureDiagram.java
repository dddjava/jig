package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.knowledge.architecture.PackageBasedArchitecture;

import java.util.StringJoiner;
import java.util.function.Function;

/**
 * アーキテクチャ図
 */
public class ArchitectureDiagram implements DiagramSourceWriter {

    PackageBasedArchitecture packageBasedArchitecture;

    public ArchitectureDiagram(PackageBasedArchitecture packageBasedArchitecture) {
        this.packageBasedArchitecture = packageBasedArchitecture;
    }

    public DiagramSources sources(JigDocumentContext jigDocumentContext) {
        ArchitectureRelations architectureRelations = ArchitectureRelations.from(packageBasedArchitecture);
        if (architectureRelations.worthless()) {
            return DiagramSource.empty();
        }

        Function<PackageId, String> architectureLabel = packageId -> jigDocumentContext.packageTerm(packageId).title();

        DocumentName documentName = DocumentName.of(JigDocument.ArchitectureDiagram);

        StringJoiner graph = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("node [shape=component,style=filled];")
                .add("graph[splines=ortho];"); // 線を直角にしておく

        // プロダクト
        graph.add("subgraph clusterArchitecture {")
                .add("label=\"\";")
                .add("graph[style=filled,color=lightgoldenrod,fillcolor=lightyellow];")
                .add("node [fillcolor=lightgoldenrod,fontsize=20];");
        for (PackageId packageId : packageBasedArchitecture.architecturePackages()) {
            graph.add(Node
                    .packageOf(packageId)
                    .label(architectureLabel.apply(packageId))
                    .asText());
        }
        graph.add("}");

        // 関連
        graph.add(RelationText.fromPackageRelations(architectureRelations.packageRelations()).asText());

        return DiagramSource.createDiagramSource(documentName, graph.toString());
    }
}
