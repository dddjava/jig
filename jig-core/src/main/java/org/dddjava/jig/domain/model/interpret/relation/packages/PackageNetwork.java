package org.dddjava.jig.domain.model.interpret.relation.packages;

import org.dddjava.jig.domain.model.declaration.package_.PackageDepth;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifiers;
import org.dddjava.jig.domain.model.diagram.*;
import org.dddjava.jig.domain.model.interpret.alias.AliasFinder;
import org.dddjava.jig.domain.model.interpret.alias.PackageAlias;
import org.dddjava.jig.domain.model.interpret.relation.class_.ClassRelation;
import org.dddjava.jig.domain.model.interpret.relation.class_.ClassRelations;
import org.dddjava.jig.presentation.view.DocumentSuffix;
import org.dddjava.jig.presentation.view.JigDocumentContext;
import org.dddjava.jig.presentation.view.JigDocumentWriter;

import java.io.OutputStreamWriter;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

/**
 * パッケージの関連
 */
public class PackageNetwork {

    PackageIdentifiers packageIdentifiers;
    PackageRelations packageRelations;
    ClassRelations classRelations;
    PackageDepth appliedDepth;
    BidirectionalRelations bidirectionalRelations;

    public PackageNetwork(PackageIdentifiers packageIdentifiers, PackageRelations packageRelations, ClassRelations classRelations) {
        this(packageIdentifiers, packageRelations, classRelations, new PackageDepth(-1));
    }

    private PackageNetwork(PackageIdentifiers packageIdentifiers, PackageRelations packageRelations, ClassRelations classRelations, PackageDepth appliedDepth) {
        this.packageIdentifiers = packageIdentifiers;
        this.packageRelations = packageRelations.filterBothMatch(packageIdentifiers);
        this.classRelations = classRelations;
        this.appliedDepth = appliedDepth;
        this.bidirectionalRelations = BidirectionalRelations.from(this.packageRelations);
    }

    public static PackageNetwork empty() {
        return new PackageNetwork(
                new PackageIdentifiers(Collections.emptyList()),
                new PackageRelations(Collections.emptyList()),
                null,
                new PackageDepth(-1)
        );
    }

    public PackageIdentifiers allPackages() {
        return packageIdentifiers;
    }

    public PackageRelations packageDependencies() {
        return packageRelations;
    }

    public PackageNetwork applyDepth(PackageDepth depth) {
        return new PackageNetwork(
                packageIdentifiers.applyDepth(depth),
                packageRelations.applyDepth(depth),
                this.classRelations,
                depth
        );
    }

    public PackageDepth appliedDepth() {
        return appliedDepth;
    }

    public boolean available() {
        return packageDependencies().available();
    }

    public PackageDepth maxDepth() {
        return packageIdentifiers.maxDepth();
    }

    public BidirectionalRelations bidirectionalRelations() {
        return bidirectionalRelations;
    }

    public boolean hasBidirectionalRelation() {
        return !bidirectionalRelations().list.isEmpty();
    }

    public String bidirectionalRelationReasonText() {
        StringJoiner sj = new StringJoiner("\n");
        for (BidirectionalRelation bidirectionalRelation : bidirectionalRelations().list) {
            sj.add("# " + bidirectionalRelation.toString());
            String package1 = bidirectionalRelation.packageRelation.from.asText();
            String package2 = bidirectionalRelation.packageRelation.to.asText();
            for (ClassRelation classRelation : classRelations.list()) {
                String from = classRelation.from().fullQualifiedName();
                String to = classRelation.to().fullQualifiedName();

                if ((from.startsWith(package1) && to.startsWith(package2))
                        || (from.startsWith(package2) && to.startsWith(package1))) {
                    sj.add("- " + classRelation.toString());
                }
            }
        }
        return sj.toString();
    }


    private String label(PackageIdentifier packageIdentifier, PackageIdentifier parent, AliasFinder aliasFinder, PackageIdentifierFormatter formatter) {
        if (!packageIdentifier.asText().startsWith(parent.asText() + '.')) {
            // TODO 通常は起こらないけれど起こらない実装にできてないので保険の実装。無くしたい。
            return label(packageIdentifier, formatter, aliasFinder);
        }
        String labelText = packageIdentifier.asText().substring(parent.asText().length() + 1);
        return addAliasIfExists(packageIdentifier, labelText, aliasFinder);
    }

    private String label(PackageIdentifier packageIdentifier, PackageIdentifierFormatter formatter, AliasFinder aliasFinder) {
        String labelText = packageIdentifier.format(formatter);
        return addAliasIfExists(packageIdentifier, labelText, aliasFinder);
    }

    private String addAliasIfExists(PackageIdentifier packageIdentifier, String labelText, AliasFinder aliasFinder) {
        PackageAlias packageAlias = aliasFinder.find(packageIdentifier);
        if (packageAlias.exists()) {
            return packageAlias.asText() + "\\n" + labelText;
        }
        return labelText;
    }


    public DotText dependencyDotText(JigDocumentContext jigDocumentContext, PackageIdentifierFormatter formatter, AliasFinder aliasFinder) {
        if (!available()) {
            return DotText.empty();
        }

        PackageRelations packageRelations = packageDependencies();
        BidirectionalRelations bidirectionalRelations = bidirectionalRelations();

        RelationText unidirectionalRelation = new RelationText("edge [color=black];");
        for (PackageRelation packageRelation : packageRelations.list()) {
            if (bidirectionalRelations.notContains(packageRelation)) {
                unidirectionalRelation.add(packageRelation.from(), packageRelation.to());
            }
        }

        Map<PackageIdentifier, List<PackageIdentifier>> map = allPackages().list().stream()
                .collect(groupingBy(PackageIdentifier::parent));

        //TODO: このとり方で良い？複数あるときはやっちゃだめじゃない？
        PackageIdentifier root = map.keySet().stream()
                .min(Comparator.comparingInt(o -> o.depth().value()))
                .orElseGet(PackageIdentifier::defaultPackage);

        StringJoiner stringJoiner = new StringJoiner("\n");
        for (PackageIdentifier parent : map.keySet()) {
            List<PackageIdentifier> children = map.get(parent);
            if (root.equals(parent)) {
                String labelsText = children.stream()
                        .map(packageIdentifier -> Node.of(packageIdentifier).label(label(packageIdentifier, formatter, aliasFinder)).asText())
                        .collect(joining("\n"));
                stringJoiner.add(labelsText);
            } else {
                String labelsText = children.stream()
                        .map(packageIdentifier -> Node.of(packageIdentifier).label(label(packageIdentifier, parent, aliasFinder, formatter)).asText())
                        .collect(joining("\n"));
                Subgraph subgraph = new Subgraph(parent.asText())
                        .add(labelsText)
                        .label(label(parent, formatter, aliasFinder))
                        .fillColor("lemonchiffon").color("lightgoldenrod").borderWidth(2);
                stringJoiner.add(subgraph.toString());
            }
        }

        String summaryText = "summary[shape=note,label=\""
                + jigDocumentContext.label("number_of_packages") + ": " + allPackages().number().asText() + "\\l"
                + jigDocumentContext.label("number_of_relations") + ": " + packageRelations.number().asText() + "\\l"
                + "\"]";

        String text = new StringJoiner("\n", "digraph {", "}")
                .add("label=\"" + jigDocumentContext.diagramLabel(JigDocument.PackageRelationDiagram) + "\";")
                .add(summaryText)
                .add(Node.DEFAULT)
                .add(unidirectionalRelation.asText())
                .add(bidirectionalRelations.dotRelationText())
                .add(stringJoiner.toString())
                .toString();
        PackageDepth packageDepth = appliedDepth();
        DocumentSuffix documentSuffix = new DocumentSuffix("-depth" + packageDepth.value());

        return new DotText(documentSuffix, text) {
            @Override
            public void additionalWrite(JigDocumentWriter jigDocumentWriter) {
                if (hasBidirectionalRelation()) {
                    jigDocumentWriter.write(
                            outputStream -> {
                                try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                                    writer.write(bidirectionalRelationReasonText());
                                }
                            },
                            "bidirectionalRelations-depth" + packageDepth + ".txt"
                    );
                }
            }
        };
    }
}
