package org.dddjava.jig.domain.model.knowledge.external;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 解析対象パッケージから外部ライブラリパッケージグループへの依存を集約した俯瞰モデル。
 * Mermaid 描画は深さ集約や JDK 表示切替を扱うためフロント側で行う。
 */
public class ExternalDependencyDiagram {

    private final Map<String, GroupNode> groups;
    private final Set<Edge> edges;

    private ExternalDependencyDiagram(Map<String, GroupNode> groups, Set<Edge> edges) {
        this.groups = groups;
        this.edges = edges;
    }

    /**
     * 解析対象から外部への参照を集約してインスタンスを構築する。
     *
     * @param externalRelations {@link TypeRelationships#externalRelation} の戻り値（解析対象→解析対象外の参照）
     * @param rule              グルーピングルール
     */
    public static ExternalDependencyDiagram from(TypeRelationships externalRelations, ExternalGroupingRule rule) {
        // groups: 出現順を保つために LinkedHashMap、edges: ソート済みかつ重複排除のため TreeSet
        Map<String, GroupNode> groups = new LinkedHashMap<>();
        Set<Edge> edges = new TreeSet<>();

        externalRelations.list().forEach(rel -> {
            // 配列型は要素型に正規化（`[Lcom.example.Foo;` → `com.example.Foo`）
            TypeId to = rel.to().unarray();
            if (to.isPrimitive() || to.isVoid()) return;
            PackageId toPackage = to.packageId();
            ExternalGroupingRule.Group group = rule.groupOf(toPackage);
            groups.computeIfAbsent(group.id(),
                            id -> new GroupNode(group.id(), group.displayName(), group.isJdk(), new TreeSet<>()))
                    .samplePackages.add(toPackage.asText());
            edges.add(new Edge(rel.from().packageId().asText(), group.id()));
        });

        return new ExternalDependencyDiagram(groups, edges);
    }

    public List<GroupNode> groups() {
        return List.copyOf(groups.values());
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

    public record GroupNode(String id, String displayName, boolean isJdk, Set<String> samplePackages) {
    }

    public record Edge(String from, String to) implements Comparable<Edge> {
        @Override
        public int compareTo(Edge o) {
            int c = from.compareTo(o.from);
            return c != 0 ? c : to.compareTo(o.to);
        }
    }
}
