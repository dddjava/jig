package org.dddjava.jig.domain.model.jigdocument.specification;

import org.dddjava.jig.application.service.AliasService;
import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.*;
import org.dddjava.jig.domain.model.parts.package_.*;
import org.dddjava.jig.domain.model.parts.relation.class_.ClassRelation;
import org.dddjava.jig.domain.model.parts.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.parts.relation.packages.BidirectionalRelation;
import org.dddjava.jig.domain.model.parts.relation.packages.BidirectionalRelations;
import org.dddjava.jig.domain.model.parts.relation.packages.PackageRelation;
import org.dddjava.jig.domain.model.parts.relation.packages.PackageRelations;

import java.util.*;

import static java.util.stream.Collectors.joining;

/**
 * パッケージ関連図
 *
 * 浅い階層は仕様記述、深い階層は実装に使用します。
 */
public class PackageRelationDiagram {

    PackageIdentifiers packageIdentifiers;
    PackageRelations packageRelations;
    ClassRelations classRelations;
    PackageDepth appliedDepth;
    BidirectionalRelations bidirectionalRelations;

    public PackageRelationDiagram(PackageIdentifiers packageIdentifiers, ClassRelations classRelations) {
        this(packageIdentifiers, PackageRelations.fromClassRelations(classRelations), classRelations, new PackageDepth(-1));
    }

    private PackageRelationDiagram(PackageIdentifiers packageIdentifiers, PackageRelations packageRelations, ClassRelations classRelations, PackageDepth appliedDepth) {
        this.packageIdentifiers = packageIdentifiers;
        this.packageRelations = packageRelations.filterBothMatch(packageIdentifiers);
        this.classRelations = classRelations;
        this.appliedDepth = appliedDepth;
        this.bidirectionalRelations = BidirectionalRelations.from(this.packageRelations);
    }

    public static PackageRelationDiagram empty() {
        return new PackageRelationDiagram(
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

    public PackageRelationDiagram applyDepth(PackageDepth depth) {
        return new PackageRelationDiagram(
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

    public DiagramSource dependencyDotText(JigDocumentContext jigDocumentContext, PackageIdentifierFormatter formatter) {
        if (!available()) {
            return DiagramSource.emptyUnit();
        }

        PackageRelations packageRelations = packageDependencies();
        BidirectionalRelations bidirectionalRelations = bidirectionalRelations();

        RelationText unidirectionalRelation = new RelationText("edge [color=black];");
        for (PackageRelation packageRelation : packageRelations.list()) {
            if (bidirectionalRelations.notContains(packageRelation)) {
                unidirectionalRelation.add(packageRelation.from(), packageRelation.to());
            }
        }

        PackageDepth maxDepth = allPackages().maxDepth();
        // 最下層を一つ上でグルーピング
        Map<PackageIdentifier, List<PackageIdentifier>> groupingPackages = new HashMap<>();
        // 最下層以外
        List<PackageIdentifier> standalonePackages = new ArrayList<>();

        for (PackageIdentifier packageIdentifier : allPackages().list()) {
            if (packageIdentifier.depth().just(maxDepth)) {
                groupingPackages.computeIfAbsent(packageIdentifier.parent(), k -> new ArrayList<>())
                        .add(packageIdentifier);
            } else {
                standalonePackages.add(packageIdentifier);
            }
        }
        // 1つにグルーピングされていたら剥がす
        if (standalonePackages.isEmpty() && groupingPackages.size() == 1) {
            for (List<PackageIdentifier> value : groupingPackages.values()) {
                standalonePackages.addAll(value);
            }
            groupingPackages.clear();
        }

        Labeler labeler = new Labeler(jigDocumentContext, formatter);

        StringJoiner stringJoiner = new StringJoiner("\n");
        for (Map.Entry<PackageIdentifier, List<PackageIdentifier>> entry : groupingPackages.entrySet()) {
            PackageIdentifier parent = entry.getKey();
            String labelsText = entry.getValue().stream()
                    .map(packageIdentifier -> Node.packageOf(packageIdentifier)
                            .label(labeler.label(packageIdentifier, parent))
                            .url(packageIdentifier, jigDocumentContext).asText())
                    .collect(joining("\n"));
            Subgraph subgraph = new Subgraph(parent.asText())
                    .add(labelsText)
                    .label(labeler.label(parent))
                    .fillColor("lemonchiffon").color("lightgoldenrod").borderWidth(2);
            stringJoiner.add(subgraph.toString());
        }
        String labelsText = standalonePackages.stream()
                .map(packageIdentifier -> Node.packageOf(packageIdentifier)
                        .label(labeler.label(packageIdentifier))
                        .url(packageIdentifier, jigDocumentContext).asText())
                .collect(joining("\n"));
        stringJoiner.add(labelsText);

        String summaryText = "summary[shape=note,label=\""
                + jigDocumentContext.label("number_of_packages") + ": " + allPackages().number().asText() + "\\l"
                + jigDocumentContext.label("number_of_relations") + ": " + packageRelations.number().asText() + "\\l"
                + "\"]";

        DocumentName documentName = jigDocumentContext.documentName(JigDocument.PackageRelationDiagram);
        String text = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add(summaryText)
                .add(Node.DEFAULT)
                .add(unidirectionalRelation.asText())
                .add(bidirectionalRelations.dotRelationText())
                .add(stringJoiner.toString())
                .toString();
        PackageDepth packageDepth = appliedDepth();

        return DiagramSource.createDiagramSourceUnit(documentName.withSuffix("-depth" + packageDepth.value()), text, additionalText());
    }

    private AdditionalText additionalText() {
        if (bidirectionalRelations().none()) {
            return AdditionalText.empty();
        }
        return new AdditionalText(bidirectionalRelationReasonText());
    }

    private String bidirectionalRelationReasonText() {
        StringJoiner sj = new StringJoiner("\n");
        for (BidirectionalRelation bidirectionalRelation : bidirectionalRelations().list()) {
            sj.add("# " + bidirectionalRelation.toString());
            for (ClassRelation classRelation : classRelations.list()) {
                if (bidirectionalRelation.matches(classRelation)) {
                    sj.add("- " + classRelation.toString());
                }
            }
        }
        return sj.toString();
    }

    static class Labeler {
        AliasService aliasService;
        PackageIdentifierFormatter packageIdentifierFormatter;

        Labeler(JigDocumentContext jigDocumentContext, PackageIdentifierFormatter packageIdentifierFormatter) {
            this.aliasService = jigDocumentContext.aliasService();
            this.packageIdentifierFormatter = packageIdentifierFormatter;
        }

        private String label(PackageIdentifier packageIdentifier, PackageIdentifier parent) {
            if (!packageIdentifier.asText().startsWith(parent.asText() + '.')) {
                // TODO 通常は起こらないけれど起こらない実装にできてないので保険の実装。無くしたい。
                return label(packageIdentifier);
            }
            // parentでくくる場合にパッケージ名をの重複を省く
            String labelText = packageIdentifier.asText().substring(parent.asText().length() + 1);
            return addAliasIfExists(packageIdentifier, labelText, aliasService);
        }

        private String label(PackageIdentifier packageIdentifier) {
            String labelText = packageIdentifier.format(packageIdentifierFormatter);
            return addAliasIfExists(packageIdentifier, labelText, aliasService);
        }

        private String addAliasIfExists(PackageIdentifier packageIdentifier, String labelText, AliasService aliasService) {
            PackageComment packageComment = aliasService.packageAliasOf(packageIdentifier);
            if (packageComment.exists()) {
                return packageComment.asText() + "\\n" + labelText;
            }
            return labelText;
        }
    }
}
