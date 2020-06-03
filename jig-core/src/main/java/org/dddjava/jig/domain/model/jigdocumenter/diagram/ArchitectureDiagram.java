package org.dddjava.jig.domain.model.jigdocumenter.diagram;

import org.dddjava.jig.domain.model.jigdocument.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.JigDocument;
import org.dddjava.jig.domain.model.jigdocumenter.DiagramSource;
import org.dddjava.jig.domain.model.jigdocumenter.DiagramSources;
import org.dddjava.jig.domain.model.jigdocumenter.JigDocumentContext;
import org.dddjava.jig.domain.model.jigdocumenter.architectures.RoundingPackageRelations;
import org.dddjava.jig.domain.model.jigmodel.Node;

import java.util.StringJoiner;

/**
 * アーキテクチャ図
 */
public class ArchitectureDiagram {

    private final RoundingPackageRelations architectureRelation;

    public ArchitectureDiagram(RoundingPackageRelations architectureRelation) {
        this.architectureRelation = architectureRelation;
    }

    public DiagramSources dotText(JigDocumentContext jigDocumentContext) {
        if (architectureRelation.worthless()) {
            return DiagramSource.empty();
        }

        DocumentName documentName = jigDocumentContext.documentName(JigDocument.ArchitectureDiagram);

        StringJoiner graph = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("subgraph clusterArchitecture {")
                .add(Node.DEFAULT)
                .add(new Node("domain").asText())
                .add(new Node("presentation").asText())
                .add(new Node("application").asText())
                .add(new Node("infrastructure").asText())
                .add("}")
                .add("label=\"" + documentName.label() + "\";")
                .add("node [shape=box,style=filled,fillcolor=whitesmoke];");
        RelationText relationText = RelationText.fromPackageRelations(architectureRelation.packageRelations());
        graph.add(relationText.asText());
        return DiagramSource.createDiagramSource(documentName, graph.toString());
    }
}
