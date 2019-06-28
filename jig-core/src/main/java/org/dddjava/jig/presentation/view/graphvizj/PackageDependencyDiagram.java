package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.declaration.package_.PackageDepth;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.declaration.package_.PackageTree;
import org.dddjava.jig.domain.model.fact.alias.AliasFinder;
import org.dddjava.jig.domain.model.fact.alias.PackageAlias;
import org.dddjava.jig.domain.model.fact.relation.packages.*;
import org.dddjava.jig.presentation.view.DocumentSuffix;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigDocumentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class PackageDependencyDiagram implements DotTextEditor<PackageNetworks> {

    static final Logger logger = LoggerFactory.getLogger(PackageDependencyDiagram.class);

    final PackageIdentifierFormatter formatter;
    final AliasFinder aliasFinder;
    JigDocumentContext jigDocumentContext;

    public PackageDependencyDiagram(PackageIdentifierFormatter formatter, AliasFinder aliasFinder) {
        this.formatter = formatter;
        this.aliasFinder = aliasFinder;
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
        for (BidirectionalRelation bidirectionalRelation : bidirectionalRelations.list()) {
            bidirectional.add(bidirectionalRelation.left(), bidirectionalRelation.right());
        }

        PackageTree tree = packageNetwork.allPackages().tree();
        PackageIdentifier root = tree.rootPackage();
        Map<PackageIdentifier, List<PackageIdentifier>> map = tree.map();
        StringJoiner stringJoiner = new StringJoiner("\n");
        for (PackageIdentifier parent : map.keySet()) {
            List<PackageIdentifier> children = map.get(parent);
            if (root.equals(parent)) {
                String labelsText = children.stream()
                        .map(packageIdentifier -> Node.of(packageIdentifier).label(label(packageIdentifier)).asText())
                        .collect(joining("\n"));
                stringJoiner.add(labelsText);
            } else {
                String labelsText = children.stream()
                        .map(packageIdentifier -> Node.of(packageIdentifier).label(label(packageIdentifier, parent)).asText())
                        .collect(joining("\n"));
                Subgraph subgraph = new Subgraph(parent.asText())
                        .add(labelsText)
                        .label(label(parent))
                        .fillColor("lemonchiffon").color("lightgoldenrod").borderWidth(2);
                stringJoiner.add(subgraph.toString());
            }
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

    private String label(PackageIdentifier packageIdentifier, PackageIdentifier parent) {
        if (!packageIdentifier.asText().startsWith(parent.asText() + '.')) {
            // TODO 通常は起こらないけれど起こらない実装にできてないので保険の実装。無くしたい。
            return label(packageIdentifier);
        }
        String labelText = packageIdentifier.asText().substring(parent.asText().length() + 1);
        return addAliasIfExists(packageIdentifier, labelText);
    }

    private String label(PackageIdentifier packageIdentifier) {
        String labelText = packageIdentifier.format(formatter);
        return addAliasIfExists(packageIdentifier, labelText);
    }

    private String addAliasIfExists(PackageIdentifier packageIdentifier, String labelText) {
        PackageAlias packageAlias = aliasFinder.find(packageIdentifier);
        if (packageAlias.exists()) {
            return packageAlias.asText() + "\\n" + labelText;
        }
        return labelText;
    }
}
