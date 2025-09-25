package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.application.CoreTypesAndRelations;
import org.dddjava.jig.domain.model.data.packages.PackageDepth;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.packages.PackageIds;
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
    private final CoreTypesAndRelations contextJigTypes;

    /**
     * 現在適用されている深さ
     */
    private final PackageDepth appliedDepth;

    public PackageRelationDiagram(PackageRelations packageRelations, CoreTypesAndRelations contextJigTypes, PackageDepth appliedDepth) {
        this.packageRelations = packageRelations;
        this.contextJigTypes = contextJigTypes;
        this.appliedDepth = appliedDepth;
    }

    public static DiagramSourceWriter from(CoreTypesAndRelations coreTypesAndRelations) {
        var packageRelations = PackageRelations.from(coreTypesAndRelations.internalTypeRelationships());
        return new PackageRelationDiagram(packageRelations, coreTypesAndRelations, new PackageDepth(-1));
    }

    /**
     * 関連なしも含むすべてのパッケージ
     */
    private PackageIds allPackages() {
        return contextJigTypes.coreJigTypes().typeIds().packageIds().applyDepth(appliedDepth);
    }

    private PackageRelationDiagram applyDepth(PackageDepth depth) {
        return new PackageRelationDiagram(
                packageRelations.applyDepth(depth),
                contextJigTypes,
                depth
        );
    }

    private Optional<DiagramSource> dependencyDotText(JigDocumentContext jigDocumentContext) {
        var relationList = packageRelations.listUnique();

        if (relationList.isEmpty()) {
            return Optional.empty();
        }

        PackageMutualDependencies packageMutualDependencies = PackageMutualDependencies.from(relationList);

        RelationText unidirectionalRelation = new RelationText("edge [color=black];");
        List<PackageRelation> filteredRelations = jigDocumentContext.diagramOption().transitiveReduction()
                ? PackageRelations.toEdges(relationList).transitiveReduction().listSortedAndConverted(PackageRelation::new)
                : relationList;
        for (PackageRelation packageRelation : filteredRelations) {
            if (packageMutualDependencies.notContains(packageRelation)) {
                unidirectionalRelation.add(packageRelation.from(), packageRelation.to());
            }
        }

        var allPackages = allPackages();
        PackageDepth maxDepth = allPackages.maxDepth();
        // 最下層を一つ上でグルーピング
        Map<PackageId, List<PackageId>> groupingPackages = new HashMap<>();
        // 最下層以外
        List<PackageId> standalonePackages = new ArrayList<>();

        for (PackageId packageId : allPackages.values()) {
            if (packageId.depth().just(maxDepth)) {
                groupingPackages.computeIfAbsent(packageId.parent(), k -> new ArrayList<>())
                        .add(packageId);
            } else {
                standalonePackages.add(packageId);
            }
        }
        // 全体が1つにグルーピングされている場合、背景色が変わるだけの意味のない構造となるので、groupingPackagesはなくす
        if (standalonePackages.isEmpty() && groupingPackages.size() == 1) {
            // 1件しかないけど全部移動するという意図でfor
            for (List<PackageId> value : groupingPackages.values()) {
                standalonePackages.addAll(value);
            }
            groupingPackages.clear();
        }

        // groupingしたパッケージ直下にクラスがある場合、standalonePackageとgroupingPackageのkeyが一致する。
        // これをgroupingの中に編入する。
        Iterator<PackageId> iterator = standalonePackages.iterator();
        while (iterator.hasNext()) {
            PackageId packageId = iterator.next();
            if (groupingPackages.containsKey(packageId)) {
                groupingPackages.get(packageId).add(packageId);
                iterator.remove();
            }
        }

        Labeler labeler = new Labeler(jigDocumentContext);
        labeler.applyContext(groupingPackages.keySet(), standalonePackages);

        String groupingSubgraphAndInternalNodeText = groupingPackages.entrySet().stream()
                .map(entry -> new Subgraph(entry.getKey().asText())
                        .addNodes(entry.getValue().stream()
                                .map(packageId -> Node.packageOf(packageId)
                                        .label(labeler.label(packageId, entry.getKey()))
                                        .url(packageId, JigDocument.DomainSummary)))
                        .label(labeler.label(entry.getKey()))
                        .fillColor("lemonchiffon").color("lightgoldenrod").borderWidth(2))
                .map(Subgraph::toString)
                .collect(joining("\n"));

        String standalonePackageNodeText = standalonePackages.stream()
                .map(packageId -> Node.packageOf(packageId)
                        .label(labeler.label(packageId))
                        .url(packageId, JigDocument.DomainSummary).dotText())
                .collect(joining("\n"));

        String summaryText = "summary[shape=note,label=\""
                + labeler.contextDescription() + "\\l"
                + allPackages.countDescriptionText() + "\\l"
                + packageRelationText(filteredRelations) + "\\l"
                + "\"]";

        DocumentName documentName = DocumentName.of(JigDocument.PackageRelationDiagram);
        String dotText = new StringJoiner("\n", "digraph \"" + documentName.label() + "\" {", "}")
                .add("label=\"" + documentName.label() + "\";")
                .add(summaryText)
                .add(Node.DEFAULT)
                .add(unidirectionalRelation.dotText())
                .add(packageMutualDependencies.dotRelationText())
                .add(groupingSubgraphAndInternalNodeText)
                .add(standalonePackageNodeText)
                .toString();

        return Optional.of(DiagramSource.createDiagramSourceUnit(documentName.withSuffix("-depth" + appliedDepth.value()), dotText, additionalText(packageMutualDependencies)));
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
            for (TypeRelationship typeRelationship : contextJigTypes.internalTypeRelationships().list()) {
                PackageRelation packageRelation = PackageRelation.from(typeRelationship.from().packageId(), typeRelationship.to().packageId());
                if (packageMutualDependency.matches(packageRelation)) {
                    sj.add("- " + typeRelationship.formatText());
                }
            }
        }
        return sj.toString();
    }

    @Override
    public DiagramSources sources(JigDocumentContext jigDocumentContext) {
        var maxDepth = packageRelations.packageIds().maxDepth();

        return DiagramSources.of(
                IntStream.rangeClosed(1, maxDepth.value())
                        .mapToObj(PackageDepth::new)
                        .map(this::applyDepth)
                        .flatMap(packageRelationDiagram -> packageRelationDiagram.dependencyDotText(jigDocumentContext).stream())
                        .toList());
    }

    public String packageRelationText(List<PackageRelation> packageRelations) {
        Locale locale = Locale.getDefault();
        boolean isEnglish = locale.getLanguage().equals("en");
        return (isEnglish ? "Relations: " : "関連数: ") + packageRelations.size();
    }
}
