package org.dddjava.jig.domain.model.jigdocument.specification;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.*;
import org.dddjava.jig.domain.model.jigmodel.architecture.ArchitectureComponent;

import java.util.StringJoiner;

/**
 * アーキテクチャ図
 */
public class ArchitectureDiagram {

    private final ArchitectureRelations architectureRelation;

    public ArchitectureDiagram(ArchitectureRelations architectureRelation) {
        this.architectureRelation = architectureRelation;
    }

    public DiagramSources dotText(JigDocumentContext jigDocumentContext) {
        if (architectureRelation.worthless()) {
            return DiagramSource.empty();
        }

        DocumentName documentName = jigDocumentContext.documentName(JigDocument.ArchitectureDiagram);

        StringJoiner graph = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("node [shape=component,style=filled];")
                .add("graph[splines=ortho];") // 線を直角にしておく
                // プロダクト
                .add("subgraph clusterArchitecture {")
                .add("graph[style=filled,color=lightgoldenrod,fillcolor=lightyellow];")
                .add("node [fillcolor=lightgoldenrod];")
                .add(new Node(ArchitectureComponent.APPLICATION.toString()).asText())
                .add(new Node(ArchitectureComponent.BUSINESS_RULE.toString()).asText())
                .add(new Node(ArchitectureComponent.PRESENTATION.toString()).asText())
                .add(new Node(ArchitectureComponent.INFRASTRUCTURE.toString()).asText())
                .add("}")
                // 周辺
                .add("node [fillcolor=whitesmoke];")
                .add(RelationText.fromPackageRelations(architectureRelation.packageRelations()).asText());

        return DiagramSource.createDiagramSource(documentName, graph.toString());
    }
}
