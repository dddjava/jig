package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.packages.PackageDepth;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifiers;
import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.information.relation.classes.ClassRelation;
import org.dddjava.jig.domain.model.information.relation.classes.ClassRelations;
import org.dddjava.jig.domain.model.information.relation.packages.PackageMutualDependencies;
import org.dddjava.jig.domain.model.information.relation.packages.PackageMutualDependency;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelation;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelations;

import java.util.*;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * パッケージ関連図
 *
 * 浅い階層は仕様記述、深い階層は実装に使用します。
 */
public class PackageRelationDiagram implements DiagramSourceWriter {

    PackageIdentifiers packageIdentifiers;
    PackageRelations packageRelations;
    ClassRelations classRelations;
    PackageDepth appliedDepth;
    PackageMutualDependencies packageMutualDependencies;

    public PackageRelationDiagram(PackageIdentifiers packageIdentifiers, ClassRelations classRelations) {
        this(packageIdentifiers, PackageRelations.from(classRelations), classRelations, new PackageDepth(-1));
    }

    private PackageRelationDiagram(PackageIdentifiers packageIdentifiers, PackageRelations packageRelations, ClassRelations classRelations, PackageDepth appliedDepth) {
        this.packageIdentifiers = packageIdentifiers;
        this.packageRelations = packageRelations.filterBothMatch(packageIdentifiers);
        this.classRelations = classRelations;
        this.appliedDepth = appliedDepth;
        this.packageMutualDependencies = PackageMutualDependencies.from(this.packageRelations);
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

    public DiagramSource dependencyDotText(JigDocumentContext jigDocumentContext) {
        if (!packageDependencies().available()) {
            return DiagramSource.emptyUnit();
        }

        PackageRelations packageRelations = packageDependencies();
        PackageMutualDependencies packageMutualDependencies = this.packageMutualDependencies;

        RelationText unidirectionalRelation = new RelationText("edge [color=black];");
        for (PackageRelation packageRelation : packageRelations.list()) {
            if (packageMutualDependencies.notContains(packageRelation)) {
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
        // 全体が1つにグルーピングされている場合、背景色が変わるだけの意味のない構造となるので、groupingPackagesはなくす
        if (standalonePackages.isEmpty() && groupingPackages.size() == 1) {
            // 1件しかないけど全部移動するという意図でfor
            for (List<PackageIdentifier> value : groupingPackages.values()) {
                standalonePackages.addAll(value);
            }
            groupingPackages.clear();
        }

        // groupingしたパッケージ直下にクラスがある場合、standalonePackageとgroupingPackageのkeyが一致する。
        // これをgroupingの中に編入する。
        Iterator<PackageIdentifier> iterator = standalonePackages.iterator();
        while (iterator.hasNext()) {
            PackageIdentifier packageIdentifier = iterator.next();
            if (groupingPackages.containsKey(packageIdentifier)) {
                groupingPackages.get(packageIdentifier).add(packageIdentifier);
                iterator.remove();
            }
        }

        Labeler labeler = new Labeler(jigDocumentContext);
        labeler.applyContext(groupingPackages.keySet(), standalonePackages);

        String groupingSubgraphAndInternalNodeText = groupingPackages.entrySet().stream()
                .map(entry -> new Subgraph(entry.getKey().asText())
                        .addNodes(entry.getValue().stream()
                                .map(packageIdentifier -> Node.packageOf(packageIdentifier)
                                        .label(labeler.label(packageIdentifier, entry.getKey()))
                                        .url(packageIdentifier, JigDocument.DomainSummary)))
                        .label(labeler.label(entry.getKey()))
                        .fillColor("lemonchiffon").color("lightgoldenrod").borderWidth(2))
                .map(Subgraph::toString)
                .collect(joining("\n"));

        String standalonePackageNodeText = standalonePackages.stream()
                .map(packageIdentifier -> Node.packageOf(packageIdentifier)
                        .label(labeler.label(packageIdentifier))
                        .url(packageIdentifier, JigDocument.DomainSummary).asText())
                .collect(joining("\n"));

        String summaryText = "summary[shape=note,label=\""
                + labeler.contextDescription() + "\\l"
                + allPackages().number().localizedLabel() + "\\l"
                + packageRelations.number().localizedLabel() + "\\l"
                + "\"]";

        DocumentName documentName = DocumentName.of(JigDocument.PackageRelationDiagram);
        String text = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add(summaryText)
                .add(Node.DEFAULT)
                .add(unidirectionalRelation.asText())
                .add(packageMutualDependencies.dotRelationText())
                .add(groupingSubgraphAndInternalNodeText)
                .add(standalonePackageNodeText)
                .toString();

        return DiagramSource.createDiagramSourceUnit(documentName.withSuffix("-depth" + appliedDepth.value()), text, additionalText());
    }

    private AdditionalText additionalText() {
        if (packageMutualDependencies.none()) {
            return AdditionalText.empty();
        }
        return new AdditionalText(bidirectionalRelationReasonText());
    }

    private String bidirectionalRelationReasonText() {
        StringJoiner sj = new StringJoiner("\n");
        for (PackageMutualDependency packageMutualDependency : packageMutualDependencies.list()) {
            sj.add("# " + packageMutualDependency.toString());
            for (ClassRelation classRelation : classRelations.list()) {
                PackageRelation packageRelation = new PackageRelation(classRelation.from().packageIdentifier(), classRelation.to().packageIdentifier());
                if (packageMutualDependency.matches(packageRelation)) {
                    sj.add("- " + classRelation.formatText());
                }
            }
        }
        return sj.toString();
    }

    /**
     * TODO 浅 -> 深の順で並べる
     */
    @Override
    public DiagramSources sources(JigDocumentContext jigDocumentContext) {
        List<PackageDepth> depths = packageIdentifiers.maxDepth().surfaceList();

        List<DiagramSource> diagramSources = depths.stream()
                .map(this::applyDepth)
                .map(packageRelationDiagram -> packageRelationDiagram.dependencyDotText(jigDocumentContext))
                .filter(diagramSource -> !diagramSource.noValue())
                .collect(toList());
        return DiagramSource.createDiagramSource(diagramSources);
    }

}
