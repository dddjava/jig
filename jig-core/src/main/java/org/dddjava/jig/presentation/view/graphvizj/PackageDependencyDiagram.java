package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.identifier.namespace.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.japanese.PackageJapaneseName;
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
        RelationText relationText = new RelationText();
        for (PackageDependency packageDependency : packageDependencies.list()) {
            relationText.add(packageDependency.from(), packageDependency.to());
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
                .add(relationText.asText())
                .add(labelsText)
                .toString();
    }
}
