package org.dddjava.jig.domain.model.jigdocument.specification;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.*;
import org.dddjava.jig.domain.model.jigmodel.architecture.ArchitectureComponents;
import org.dddjava.jig.domain.model.parts.alias.AliasFinder;
import org.dddjava.jig.domain.model.parts.alias.PackageAlias;
import org.dddjava.jig.domain.model.parts.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.parts.relation.class_.ClassRelations;

import java.util.StringJoiner;
import java.util.function.Function;

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
        AliasFinder aliasFinder = jigDocumentContext.aliasFinder();
        Function<PackageIdentifier, String> architectureLabel = packageIdentifier -> {
            PackageAlias packageAlias = aliasFinder.find(packageIdentifier);
            return packageAlias.exists() ? packageAlias.asText() : packageIdentifier.simpleName();
        };

        DocumentName documentName = jigDocumentContext.documentName(JigDocument.ArchitectureDiagram);

        StringJoiner graph = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add("node [shape=component,style=filled];")
                .add("graph[splines=ortho];"); // 線を直角にしておく

        // プロダクト
        graph.add("subgraph clusterArchitecture {")
                .add("label=\"\";")
                .add("graph[style=filled,color=lightgoldenrod,fillcolor=lightyellow];")
                .add("node [fillcolor=lightgoldenrod,fontsize=20];");
        for (PackageIdentifier packageIdentifier : architectureComponents.architecturePackages()) {
            graph.add(Node
                    .packageOf(packageIdentifier)
                    .label(architectureLabel.apply(packageIdentifier))
                    .asText());
        }
        graph.add("}");

        // 関連
        graph.add(RelationText.fromPackageRelations(architectureRelations.packageRelations()).asText());

        return DiagramSource.createDiagramSource(documentName, graph.toString());
    }
}
