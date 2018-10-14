package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;
import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.japanese.PackageJapaneseName;
import org.dddjava.jig.domain.model.networks.packages.*;
import org.dddjava.jig.presentation.view.DocumentSuffix;

import java.util.List;
import java.util.StringJoiner;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class PackageDependencyDiagram implements DotTextEditor<PackageNetworks> {

    final PackageIdentifierFormatter formatter;
    final JapaneseNameFinder japaneseNameFinder;

    public PackageDependencyDiagram(PackageIdentifierFormatter formatter, JapaneseNameFinder japaneseNameFinder) {
        this.formatter = formatter;
        this.japaneseNameFinder = japaneseNameFinder;
    }

    @Override
    public DotTexts edit(PackageNetworks packageNetworks) {
        List<DotText> values = packageNetworks.list().stream()
                .map(this::toDotText)
                .collect(toList());
        return new DotTexts(values);
    }

    private DotText toDotText(PackageNetwork packageNetwork) {
        PackageDependencies packageDependencies = packageNetwork.packageDependencies();

        BidirectionalDependencies bidirectionalDependencies = BidirectionalDependencies.from(packageDependencies);

        RelationText unidirectionalRelation = new RelationText("edge [color=black];");
        PackageDependencies unidirectionalDependencies = bidirectionalDependencies.filterBidirectionalFrom(packageDependencies);
        for (PackageDependency packageDependency : unidirectionalDependencies.list()) {
            unidirectionalRelation.add(packageDependency.from(), packageDependency.to());
        }

        RelationText bidirectional = new RelationText("edge [color=red,dir=both,style=bold];");
        for (BidirectionalDependency packageDependency : bidirectionalDependencies.list()) {
            bidirectional.add(packageDependency.left(), packageDependency.right());
        }

        String labelsText = packageNetwork.allPackages().stream()
                .map(packageIdentifier -> {
                    String labelText = packageIdentifier.format(formatter);
                    PackageJapaneseName packageJapaneseName = japaneseNameFinder.find(packageIdentifier);
                    if (packageJapaneseName.exists()) {
                        labelText = packageJapaneseName.japaneseName().summarySentence() + "\\n" + labelText;
                    }
                    return Node.of(packageIdentifier)
                            .label(labelText).asText();
                })
                .collect(joining("\n"));

        String summaryText = "summary[shape=note,label=\""
                + "パッケージ数: " + packageNetwork.allPackages().number().asText() + "\\l"
                + "関連数: " + packageDependencies.number().asText() + "\\l"
                + "\"]";

        String text = new StringJoiner("\n", "digraph {", "}")
                .add(summaryText)
                .add("node [shape=box,style=filled,fillcolor=lightgoldenrod];")
                .add(unidirectionalRelation.asText())
                .add(bidirectional.asText())
                .add(labelsText)
                .toString();
        PackageDepth packageDepth = packageNetwork.appliedDepth();
        DocumentSuffix documentSuffix = new DocumentSuffix(packageDepth.unlimited() ? "" : "-depth" + packageDepth.value());
        return new DotText(documentSuffix, text);
    }
}
