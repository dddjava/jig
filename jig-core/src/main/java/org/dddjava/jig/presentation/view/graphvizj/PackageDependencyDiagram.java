package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.namespace.PackageDepth;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.namespace.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.namespace.PackageTree;
import org.dddjava.jig.domain.model.implementation.analyzed.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.implementation.analyzed.japanese.PackageJapaneseName;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.packages.*;
import org.dddjava.jig.presentation.view.DocumentSuffix;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigDocumentContext;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class PackageDependencyDiagram implements DotTextEditor<PackageNetworks> {

    final PackageIdentifierFormatter formatter;
    final JapaneseNameFinder japaneseNameFinder;
    JigDocumentContext jigDocumentContext;

    public PackageDependencyDiagram(PackageIdentifierFormatter formatter, JapaneseNameFinder japaneseNameFinder) {
        this.formatter = formatter;
        this.japaneseNameFinder = japaneseNameFinder;
        this.jigDocumentContext = JigDocumentContext.getInstance();
    }

    @Override
    public DotTexts edit(PackageNetworks packageNetworks) {
        List<DotText> values = packageNetworks.list().stream()
                .map(this::toDotText)
                .collect(toList());
        return new DotTexts(values);
    }

    private DotText toDotText(PackageNetwork packageNetwork) {
        PackageRelations packageRelations = packageNetwork.packageDependencies();

        BidirectionalRelations bidirectionalRelations = BidirectionalRelations.from(packageRelations);

        RelationText unidirectionalRelation = new RelationText("edge [color=black];");
        PackageRelations unidirectionalDependencies = bidirectionalRelations.filterBidirectionalFrom(packageRelations);
        for (PackageRelation packageRelation : unidirectionalDependencies.list()) {
            unidirectionalRelation.add(packageRelation.from(), packageRelation.to());
        }

        RelationText bidirectional = new RelationText("edge [color=red,dir=both,style=bold];");
        for (BidirectionalRelation packageDependency : bidirectionalRelations.list()) {
            bidirectional.add(packageDependency.left(), packageDependency.right());
        }

        PackageTree tree = packageNetwork.allPackages().tree();
        Map<PackageIdentifier, List<PackageIdentifier>> map = tree.map();
        StringJoiner stringJoiner = new StringJoiner("\n");
        for(PackageIdentifier parent : map.keySet()) {
            List<PackageIdentifier> children = map.get(parent);
            String labelsText = children.stream()
                    .map(packageIdentifier -> {
                        String labelText = label(packageIdentifier);
                        return Node.of(packageIdentifier)
                                .label(labelText).asText();
                    })
                    .collect(joining("\n"));
            Subgraph subgraph = new Subgraph(parent.asText()).add(labelsText).label(label(parent)).fillColor("lemonchiffon").color("lightgoldenrod").borderWidth(2);
            stringJoiner.add(subgraph.toString());
        }

        String summaryText = "summary[shape=note,label=\""
                + jigDocumentContext.label("number_of_packages") + ": " + packageNetwork.allPackages().number().asText() + "\\l"
                + jigDocumentContext.label("number_of_relations") + ": " + packageRelations.number().asText() + "\\l"
                + "\"]";

        String text = new StringJoiner("\n", "digraph {", "}")
                .add("label=\"" + jigDocumentContext.diagramLabel(JigDocument.PackageRelationDiagram) + "\";")
                .add(summaryText)
                .add(Node.DEFAULT)
                .add(unidirectionalRelation.asText())
                .add(bidirectional.asText())
                .add(stringJoiner.toString())
                .toString();
        PackageDepth packageDepth = packageNetwork.appliedDepth();
        DocumentSuffix documentSuffix = new DocumentSuffix("-depth" + packageDepth.value());
        return new DotText(documentSuffix, text);
    }

    private String label(PackageIdentifier packageIdentifier) {
        String labelText = packageIdentifier.format(formatter);
        PackageJapaneseName packageJapaneseName = japaneseNameFinder.find(packageIdentifier);
        if (packageJapaneseName.exists()) {
            labelText = packageJapaneseName.japaneseName().summarySentence() + "\\n" + labelText;
        }
        return labelText;
    }
}
