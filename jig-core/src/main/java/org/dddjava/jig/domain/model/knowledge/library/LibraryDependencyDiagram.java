package org.dddjava.jig.domain.model.knowledge.library;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 解析対象パッケージから外部ライブラリへの依存を集約した俯瞰モデル。
 * Mermaid 描画は深さ集約や Java 標準の表示切替を扱うためフロント側で行う。
 */
public class LibraryDependencyDiagram {

    private final Map<String, LibraryNode> libraries;
    private final Set<Edge> edges;

    private LibraryDependencyDiagram(Map<String, LibraryNode> libraries, Set<Edge> edges) {
        this.libraries = libraries;
        this.edges = edges;
    }

    /**
     * 解析対象から外部への参照を集約してインスタンスを構築する。
     *
     * @param externalRelations {@link TypeRelationships#externalRelation} の戻り値（解析対象→解析対象外の参照）
     * @param rule              ライブラリ識別ルール
     */
    public static LibraryDependencyDiagram from(TypeRelationships externalRelations, LibraryGroupingRule rule) {
        // libraries: 出現順を保つために LinkedHashMap、edges: ソート済みかつ重複排除のため TreeSet
        Map<String, LibraryNodeBuilder> builders = new LinkedHashMap<>();
        Set<Edge> edges = new TreeSet<>();

        externalRelations.list().forEach(rel -> {
            // 配列型は要素型に正規化（`[Lcom.example.Foo;` → `com.example.Foo`）
            TypeId to = rel.to().unarray();
            if (to.isPrimitive() || to.isVoid()) return;
            PackageId toPackage = to.packageId();
            // パッケージなし（`T` `E` などのジェネリクス型変数や default package）は外部依存として扱わない
            if (toPackage.equals(PackageId.defaultPackage())) return;
            LibraryGroupingRule.Library library = rule.libraryOf(toPackage);
            LibraryNodeBuilder builder = builders.computeIfAbsent(library.id(), id -> new LibraryNodeBuilder(library));
            builder.samplePackages.add(toPackage.asText());
            builder.usingClasses.add(rel.from().fqn());
            edges.add(new Edge(rel.from().packageId().asText(), library.id()));
        });

        Map<String, LibraryNode> libraries = new LinkedHashMap<>();
        builders.forEach((id, builder) -> libraries.put(id, builder.build()));
        return new LibraryDependencyDiagram(libraries, edges);
    }

    private static final class LibraryNodeBuilder {
        private final LibraryGroupingRule.Library library;
        private final Set<String> samplePackages = new TreeSet<>();
        private final Set<String> usingClasses = new TreeSet<>();

        LibraryNodeBuilder(LibraryGroupingRule.Library library) {
            this.library = library;
        }

        LibraryNode build() {
            return new LibraryNode(library.id(), library.displayName(), library.isJavaStandard(),
                    List.copyOf(samplePackages), List.copyOf(usingClasses));
        }
    }

    public List<LibraryNode> libraries() {
        return List.copyOf(libraries.values());
    }

    public List<Edge> edges() {
        return List.copyOf(edges);
    }

    public Set<String> internalPackageFqns() {
        Set<String> set = new TreeSet<>();
        for (Edge edge : edges) {
            set.add(edge.from);
        }
        return set;
    }

    public record LibraryNode(String id, String displayName, boolean isJavaStandard,
                              List<String> samplePackages, List<String> usingClasses) {
    }

    public record Edge(String from, String to) implements Comparable<Edge> {
        @Override
        public int compareTo(Edge o) {
            int c = from.compareTo(o.from);
            return c != 0 ? c : to.compareTo(o.to);
        }
    }
}
