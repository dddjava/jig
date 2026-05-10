package org.dddjava.jig.domain.model.knowledge.external;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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

        // 内部パッケージは階層構造（共通親パッケージ）で subgraph グルーピング
        List<String> parentSelfNodeIds = new ArrayList<>();
        PackageTree.of(internalPackageFqns()).render(sb, 1, parentSelfNodeIds);

        // 外部グループはフラットに並べる（非 JDK 先、JDK 後）
        groups.values().stream()
                .filter(g -> includeJdk || !g.isJdk)
                .sorted((a, b) -> a.isJdk == b.isJdk ? 0 : a.isJdk ? 1 : -1)
                .forEach(node -> sb.append("    ").append(nodeId(node.id))
                        .append("([\"").append(escape(node.displayName)).append("\"])\n"));

        for (Edge edge : edges) {
            GroupNode toNode = groups.get(edge.to);
            if (toNode == null || (!includeJdk && toNode.isJdk)) continue;
            sb.append("    ").append(nodeId(edge.from))
                    .append(" --> ").append(nodeId(edge.to)).append("\n");
        }

        // 既存のパッケージ関連図と同じ親パッケージスタイル（破線枠）
        sb.append("    classDef parentPackage fill:#ffffce,stroke:#aaaa00,stroke-dasharray:10 3\n");
        for (String selfId : parentSelfNodeIds) {
            sb.append("    class ").append(selfId).append(" parentPackage\n");
        }
        return sb.toString();
    }

    /**
     * パッケージ FQN のセットから共通親で階層化したツリーを構築し、Mermaid の subgraph に展開する。
     * 単一の子しか持たない中間階層は subgraph を作らずに親へ折りたたむ。
     */
    private static final class PackageTree {
        private final String fqn;
        private final Map<String, PackageTree> children = new TreeMap<>();
        private boolean isLeaf;

        private PackageTree(String fqn) {
            this.fqn = fqn;
        }

        static PackageTree of(Set<String> packageFqns) {
            PackageTree root = new PackageTree("");
            packageFqns.forEach(fqn -> {
                String[] parts = fqn.split("\\.");
                PackageTree current = root;
                StringBuilder path = new StringBuilder();
                for (String part : parts) {
                    if (path.length() > 0) path.append('.');
                    path.append(part);
                    current = current.children.computeIfAbsent(path.toString(), PackageTree::new);
                }
                current.isLeaf = true;
            });
            return root;
        }

        void render(StringBuilder sb, int indent, List<String> parentSelfNodeIds) {
            children.values().forEach(child -> child.renderNode(sb, indent, "", parentSelfNodeIds));
        }

        private void renderNode(StringBuilder sb, int indent, String parentFqn, List<String> parentSelfNodeIds) {
            String label = displayLabel(parentFqn);
            if (children.isEmpty()) {
                // 既存パッケージ図と同じ shape: st-rect
                appendIndent(sb, indent).append(nodeId(fqn))
                        .append("@{shape: st-rect, label: \"").append(escape(label)).append("\"}\n");
                return;
            }
            // 子が一つで自身が leaf でないなら subgraph を省いて子へ降りる（パス圧縮）
            if (!isLeaf && children.size() == 1) {
                children.values().iterator().next().renderNode(sb, indent, parentFqn, parentSelfNodeIds);
                return;
            }
            // subgraph ID は leaf ノード ID と衝突しないよう接尾辞を付ける
            String groupId = nodeId(fqn) + "_grp";
            appendIndent(sb, indent).append("subgraph ").append(groupId)
                    .append(" [\"").append(escape(label)).append("\"]\n");
            if (isLeaf) {
                // 自身パッケージにも直接クラスがある（親かつリーフ）：パッケージ FQN をラベルにし、
                // 既存パッケージ図と同様に parentPackage クラスで破線枠スタイルを適用
                String selfId = nodeId(fqn);
                appendIndent(sb, indent + 1).append(selfId)
                        .append("@{shape: st-rect, label: \"").append(escape(fqn)).append("\"}\n");
                parentSelfNodeIds.add(selfId);
            }
            children.values().forEach(child -> child.renderNode(sb, indent + 1, fqn, parentSelfNodeIds));
            appendIndent(sb, indent).append("end\n");
        }

        private String displayLabel(String parentFqn) {
            if (!parentFqn.isEmpty() && fqn.startsWith(parentFqn + ".")) {
                return fqn.substring(parentFqn.length() + 1);
            }
            return fqn;
        }

        private static StringBuilder appendIndent(StringBuilder sb, int level) {
            for (int i = 0; i < level; i++) sb.append("    ");
            return sb;
        }
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
