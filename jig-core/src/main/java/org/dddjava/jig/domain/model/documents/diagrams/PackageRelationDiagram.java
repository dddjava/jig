package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;
import org.dddjava.jig.domain.model.parts.classes.type.ClassRelation;
import org.dddjava.jig.domain.model.parts.classes.type.ClassRelations;
import org.dddjava.jig.domain.model.parts.packages.*;

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
    BidirectionalRelations bidirectionalRelations;

    public PackageRelationDiagram(PackageIdentifiers packageIdentifiers, ClassRelations classRelations) {
        this(packageIdentifiers, classRelations.toPackageRelations(), classRelations, new PackageDepth(-1));
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

    public DiagramSource dependencyDotText(JigDocumentContext jigDocumentContext) {
        if (!packageDependencies().available()) {
            return DiagramSource.emptyUnit();
        }

        PackageRelations packageRelations = packageDependencies();
        BidirectionalRelations bidirectionalRelations = this.bidirectionalRelations;

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

        Labeler labeler = new Labeler(jigDocumentContext);
        labeler.applyContext(groupingPackages.keySet(), standalonePackages);

        StringJoiner stringJoiner = new StringJoiner("\n");
        for (Map.Entry<PackageIdentifier, List<PackageIdentifier>> entry : groupingPackages.entrySet()) {
            PackageIdentifier parent = entry.getKey();
            String labelsText = entry.getValue().stream()
                    .map(packageIdentifier -> Node.packageOf(packageIdentifier)
                            .label(labeler.label(packageIdentifier, parent))
                            .url(packageIdentifier, jigDocumentContext, JigDocument.DomainSummary).asText())
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
                        .url(packageIdentifier, jigDocumentContext, JigDocument.DomainSummary).asText())
                .collect(joining("\n"));
        stringJoiner.add(labelsText);

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
                .add(bidirectionalRelations.dotRelationText())
                .add(stringJoiner.toString())
                .toString();

        return DiagramSource.createDiagramSourceUnit(documentName.withSuffix("-depth" + appliedDepth.value()), text, additionalText());
    }

    private AdditionalText additionalText() {
        if (bidirectionalRelations.none()) {
            return AdditionalText.empty();
        }
        return new AdditionalText(bidirectionalRelationReasonText());
    }

    private String bidirectionalRelationReasonText() {
        StringJoiner sj = new StringJoiner("\n");
        for (BidirectionalRelation bidirectionalRelation : bidirectionalRelations.list()) {
            sj.add("# " + bidirectionalRelation.toString());
            for (ClassRelation classRelation : classRelations.list()) {
                PackageRelation packageRelation = new PackageRelation(classRelation.from().packageIdentifier(), classRelation.to().packageIdentifier());
                if (bidirectionalRelation.matches(packageRelation)) {
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

    static class Labeler {
        JigDocumentContext jigDocumentContext;
        private Optional<String> commonPrefix = Optional.empty();

        Labeler(JigDocumentContext jigDocumentContext) {
            this.jigDocumentContext = jigDocumentContext;
        }

        private String label(PackageIdentifier packageIdentifier, PackageIdentifier parent) {
            if (!packageIdentifier.asText().startsWith(parent.asText() + '.')) {
                // 引数の食い違いがあった場合に予期しない編集を行わないための回避コード。
                // TODO 通常は起こらないけれど起こらない実装にできてないので保険の実装。無くしたい。
                return label(packageIdentifier);
            }
            // parentでくくる場合にパッケージ名をの重複を省く
            String labelText = packageIdentifier.asText().substring(parent.asText().length());
            return addAliasIfExists(packageIdentifier, trimDot(labelText));
        }

        private String label(PackageIdentifier packageIdentifier) {
            String fqpn = packageIdentifier.asText();

            System.out.println(commonPrefix + " : " + fqpn);
            String labelText = commonPrefix.map(String::length).map(index -> trimDot(fqpn.substring(index)))
                    .orElse(fqpn);

            return addAliasIfExists(packageIdentifier, labelText);
        }

        private String addAliasIfExists(PackageIdentifier packageIdentifier, String labelText) {
            PackageComment packageComment = jigDocumentContext.packageComment(packageIdentifier);
            if (packageComment.exists()) {
                return packageComment.asText() + "\\n" + labelText;
            }
            return labelText;
        }

        void applyContext(Collection<PackageIdentifier> groupingPackages, List<PackageIdentifier> allStandalonePackageIdentifiers) {
            // groupingPackagesとallStandalonePackageIdentifiersをまとめる
            Collection<PackageIdentifier> collection = new HashSet<>();
            collection.addAll(groupingPackages);
            collection.addAll(allStandalonePackageIdentifiers);

            // 引数が空ならreturn
            if (collection.isEmpty()) {
                return;
            }

            // 全てで共通する部分を抜き出す
            String sameRootText = null;
            for (PackageIdentifier currentPackageIdentifier : collection) {
                PackageIdentifier currentParentPackageIdentifier = currentPackageIdentifier.parent();
                String currentText = currentParentPackageIdentifier.asText();
                if (sameRootText == null) {
                    sameRootText = currentText;
                    continue;
                }

                // sameRootTextとcurrentTextで前方から一致する部分の文字列を求める
                int commonPrefixLength = 0;
                for (int i = 0; i < Math.min(sameRootText.length(), currentText.length()); i++) {
                    if (sameRootText.charAt(i) == currentText.charAt(i)) {
                        commonPrefixLength++;
                    } else {
                        break;
                    }
                }

                sameRootText = sameRootText.substring(0, commonPrefixLength);
            }

            this.commonPrefix = Optional.of(trimDot(sameRootText));
        }

        public String contextDescription() {
            return "root: " + commonPrefix.orElse("(none)");
        }

        private String trimDot(String string) {
            return string.replaceAll("^\\.|\\.$", "");
        }
    }
}
