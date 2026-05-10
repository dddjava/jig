package org.dddjava.jig.domain.model.knowledge.external;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * 解析対象パッケージから外部ライブラリパッケージグループへの依存を集約した俯瞰モデル。
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

    /**
     * Mermaid 記法のテキストを生成する。
     *
     * @param includeJdk JDK（java.*, javax.*）由来のグループを含めるかどうか
     */
    public String mermaidText(boolean includeJdk) {
        StringBuilder sb = new StringBuilder();
        sb.append("flowchart LR\n");

        for (String fqn : internalPackageFqns()) {
            sb.append("    ").append(nodeId(fqn))
                    .append("[\"").append(escape(fqn)).append("\"]\n");
        }

        List<GroupNode> visibleGroups = visibleGroups(includeJdk).toList();
        if (!visibleGroups.isEmpty()) {
            sb.append("    subgraph external [\"外部\"]\n");
            // 非 JDK を先、JDK を後に表示
            Stream.concat(
                            visibleGroups.stream().filter(g -> !g.isJdk),
                            visibleGroups.stream().filter(g -> g.isJdk))
                    .forEach(node -> sb.append("        ").append(nodeId(node.id))
                            .append("([\"").append(escape(node.displayName)).append("\"])\n"));
            sb.append("    end\n");
        }

        for (Edge edge : edges) {
            GroupNode toNode = groups.get(edge.to);
            if (toNode == null || (!includeJdk && toNode.isJdk)) continue;
            sb.append("    ").append(nodeId(edge.from))
                    .append(" --> ").append(nodeId(edge.to)).append("\n");
        }
        return sb.toString();
    }

    private Stream<GroupNode> visibleGroups(boolean includeJdk) {
        return groups.values().stream().filter(g -> includeJdk || !g.isJdk);
    }

    private static String nodeId(String text) {
        return "n_" + text.replaceAll("[^A-Za-z0-9]", "_");
    }

    private static String escape(String text) {
        return text.replace("\"", "\\\"");
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
