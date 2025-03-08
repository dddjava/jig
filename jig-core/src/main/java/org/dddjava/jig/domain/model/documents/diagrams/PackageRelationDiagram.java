package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.application.JigTypesWithRelationships;
import org.dddjava.jig.domain.model.data.packages.PackageDepth;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifiers;
import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.information.relation.packages.PackageMutualDependencies;
import org.dddjava.jig.domain.model.information.relation.packages.PackageMutualDependency;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelation;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelations;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationship;

import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

/**
 * パッケージ関連図
 *
 * 浅い階層は仕様記述、深い階層は実装に使用します。
 */
public class PackageRelationDiagram implements DiagramSourceWriter {

    /**
     * 出力対象の関連
     */
    private final PackageRelations packageRelations;

    /**
     * コンテキストとなるJigType
     * 依存なしのパッケージや相互参照場合の原因となるクラス関連を出力するため
     */
    private final JigTypesWithRelationships contextJigTypes;

    /**
     * 現在適用されている深さ
     */
    private final PackageDepth appliedDepth;

    public PackageRelationDiagram(PackageRelations packageRelations, JigTypesWithRelationships contextJigTypes, PackageDepth appliedDepth) {
        this.packageRelations = packageRelations;
        this.contextJigTypes = contextJigTypes;
        this.appliedDepth = appliedDepth;
    }

    public static PackageRelationDiagram empty() {
        return new PackageRelationDiagram(
                new PackageRelations(Collections.emptyList()),
                null,
                new PackageDepth(-1)
        );
    }

    public static DiagramSourceWriter from(JigTypesWithRelationships jigTypesWithRelationships) {
        var packageRelations = PackageRelations.from(jigTypesWithRelationships.typeRelationships());
        return new PackageRelationDiagram(packageRelations, jigTypesWithRelationships, new PackageDepth(-1));
    }

    /**
     * 関連なしも含むすべてのパッケージ
     */
    public PackageIdentifiers allPackages() {
        return contextJigTypes.jigTypes().typeIdentifiers().packageIdentifiers().applyDepth(appliedDepth);
    }

    public PackageRelationDiagram applyDepth(PackageDepth depth) {
        return new PackageRelationDiagram(
                packageRelations.applyDepth(depth),
                contextJigTypes,
                depth
        );
    }

    public DiagramSource dependencyDotText(JigDocumentContext jigDocumentContext) {
        var relationList = packageRelations.listUnique();

        if (relationList.isEmpty()) {
            return DiagramSource.emptyUnit();
        }

        PackageMutualDependencies packageMutualDependencies = PackageMutualDependencies.from(relationList);

        RelationText unidirectionalRelation = new RelationText("edge [color=black];");
        List<PackageRelation> filteredRelations = jigDocumentContext.diagramOption().transitiveReduction()
                ? PackageRelations.fromPackageRelations(relationList).transitiveReduction().convert(PackageRelation::from)
                : relationList;
        for (PackageRelation packageRelation : filteredRelations) {
            if (packageMutualDependencies.notContains(packageRelation)) {
                unidirectionalRelation.add(packageRelation.from(), packageRelation.to());
            }
        }

        var allPackages = allPackages();
        PackageDepth maxDepth = allPackages.maxDepth();
        // 最下層を一つ上でグルーピング
        Map<PackageIdentifier, List<PackageIdentifier>> groupingPackages = new HashMap<>();
        // 最下層以外
        List<PackageIdentifier> standalonePackages = new ArrayList<>();

        for (PackageIdentifier packageIdentifier : allPackages.list()) {
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
                + allPackages.number().localizedLabel() + "\\l"
                + packageRelationText(filteredRelations) + "\\l"
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

        return DiagramSource.createDiagramSourceUnit(documentName.withSuffix("-depth" + appliedDepth.value()), text, additionalText(packageMutualDependencies));
    }

    private AdditionalText additionalText(PackageMutualDependencies packageMutualDependencies) {
        if (packageMutualDependencies.none()) {
            return AdditionalText.empty();
        }
        return new AdditionalText(bidirectionalRelationReasonText(packageMutualDependencies));
    }

    private String bidirectionalRelationReasonText(PackageMutualDependencies packageMutualDependencies) {
        StringJoiner sj = new StringJoiner("\n");
        for (PackageMutualDependency packageMutualDependency : packageMutualDependencies.list()) {
            sj.add("# " + packageMutualDependency.toString());
            for (TypeRelationship typeRelationship : contextJigTypes.typeRelationships().list()) {
                PackageRelation packageRelation = PackageRelation.from(typeRelationship.from().packageIdentifier(), typeRelationship.to().packageIdentifier());
                if (packageMutualDependency.matches(packageRelation)) {
                    sj.add("- " + typeRelationship.formatText());
                }
            }
        }
        return sj.toString();
    }

    @Override
    public DiagramSources sources(JigDocumentContext jigDocumentContext) {
        var maxDepth = packageRelations.packageIdentifiers().maxDepth();

        return DiagramSource.createDiagramSource(
                IntStream.rangeClosed(1, maxDepth.value())
                        .mapToObj(PackageDepth::new)
                        .map(this::applyDepth)
                        .map(packageRelationDiagram -> packageRelationDiagram.dependencyDotText(jigDocumentContext))
                        .filter(diagramSource -> !diagramSource.noValue())
                        .toList());
    }

    public String packageRelationText(List<PackageRelation> packageRelations) {
        Locale locale = Locale.getDefault();
        boolean isEnglish = locale.getLanguage().equals("en");
        return (isEnglish ? "Relations: " : "関連数: ") + packageRelations.size();
    }
}
