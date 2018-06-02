package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.identifier.namespace.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.japanese.PackageJapaneseName;
import org.dddjava.jig.domain.model.networks.BidirectionalDependencies;
import org.dddjava.jig.domain.model.networks.BidirectionalDependency;
import org.dddjava.jig.domain.model.networks.PackageDependencies;
import org.dddjava.jig.domain.model.networks.PackageDependency;

import java.util.StringJoiner;

import static java.util.stream.Collectors.joining;

public class PackageDependencyDiagram implements DotTextEditor<PackageDependencies> {

    final PackageIdentifierFormatter formatter;
    final JapaneseNameFinder japaneseNameFinder;

    public PackageDependencyDiagram(PackageIdentifierFormatter formatter, JapaneseNameFinder japaneseNameFinder) {
        this.formatter = formatter;
        this.japaneseNameFinder = japaneseNameFinder;
    }

    @Override
    public String edit(PackageDependencies packageDependencies) {

        BidirectionalDependencies bidirectionalDependencies = BidirectionalDependencies.from(packageDependencies);

        RelationText unidirectionalRelation = new RelationText("edge [color=black];");
        PackageDependencies unidirectionalDependencies = bidirectionalDependencies.filterBidirectionalFrom(packageDependencies);
        for (PackageDependency packageDependency : unidirectionalDependencies.list()) {
            unidirectionalRelation.add(packageDependency.from(), packageDependency.to());
        }

        RelationText bidirectional = new RelationText("edge [color=red];");
        for (BidirectionalDependency packageDependency : bidirectionalDependencies.list()) {
            bidirectional.add(packageDependency.left(), packageDependency.right());
            bidirectional.add(packageDependency.right(), packageDependency.left());
        }

        String labelsText = packageDependencies.allPackages().stream()
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

        return new StringJoiner("\n", "digraph {", "}")
                .add("node [shape=box,style=filled,color=lightgoldenrod];")
                .add(unidirectionalRelation.asText())
                .add(bidirectional.asText())
                .add(labelsText)
                .toString();
    }
}
