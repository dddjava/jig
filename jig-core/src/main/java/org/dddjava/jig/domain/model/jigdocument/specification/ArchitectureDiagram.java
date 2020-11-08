package org.dddjava.jig.domain.model.jigdocument.specification;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.*;
import org.dddjava.jig.domain.model.jigmodel.architecture.ArchitectureComponents;
import org.dddjava.jig.domain.model.jigmodel.architecture.ArchitectureModule;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelations;

import java.util.StringJoiner;

/**
 * アーキテクチャ図
 */
public class ArchitectureDiagram {

    ArchitectureComponents architectureComponents;
    ClassRelations classRelations;

    public ArchitectureDiagram(ArchitectureComponents architectureComponents, ClassRelations classRelations) {
        this.architectureComponents = architectureComponents;
        this.classRelations = classRelations;
    }

    public DiagramSources dotText(JigDocumentContext jigDocumentContext) {
        ArchitectureRelations architectureRelations = ArchitectureRelations.from(architectureComponents, classRelations);
        if (architectureRelations.worthless()) {
            return DiagramSource.empty();
        }

        // packageのaliasを使う
        //jigDocumentContext.aliasFinder().find(PackageIdentifier)

        DocumentName documentName = jigDocumentContext.documentName(JigDocument.ArchitectureDiagram);

        StringJoiner graph = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("node [shape=component,style=filled];")
                .add("graph[splines=ortho];"); // 線を直角にしておく

        // プロダクト
        graph.add("subgraph clusterArchitecture {")
                .add("graph[style=filled,color=lightgoldenrod,fillcolor=lightyellow];")
                .add("node [fillcolor=lightgoldenrod];");
        for (ArchitectureModule module : architectureComponents.listOwnModules()) {
            graph.add(Node.architectureModuleOf(module).asText());
        }
        graph.add("}");

        // 関連
        graph.add(RelationText.fromPackageRelations(architectureRelations.packageRelations()).asText());

        return DiagramSource.createDiagramSource(documentName, graph.toString());
    }
}
