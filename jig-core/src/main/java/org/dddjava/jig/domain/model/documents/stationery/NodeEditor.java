package org.dddjava.jig.domain.model.documents.stationery;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ノードの最終的な編集者
 */
public class NodeEditor {
    static final NodeEditor INSTANCE = new NodeEditor();
    static final Predicate<String> 英数のみ = Pattern.compile("^0-9a-zA-Z$").asMatchPredicate();

    private NodeEditor() {
        editors = new ArrayList<>();
        // labelが * で始まったらgreenyellowにする
        editors.add(new Editor(node -> node.labelMatches(label -> label.startsWith("*")), node -> node.fillColor("greenyellow")));

        // #837 実験機能
        property("jig.highlight.label-pattern").ifPresent(pattern -> {
            String fillColor = property("jig.highlight.fillcolor").orElse("yellow");
            editors.add(new Editor(node -> node.labelMatches(Pattern.compile(pattern).asMatchPredicate()), node -> node.fillColor(fillColor)));
        });
        property("jig.highlight.identifier-pattern").ifPresent(pattern -> {
            String fillColor = property("jig.highlight.fillcolor").orElse("yellow");
            editors.add(new Editor(node -> node.identifierMatches(Pattern.compile(pattern).asMatchPredicate()), node -> node.fillColor(fillColor)));
        });
    }

    static Optional<String> property(String key) {
        return Optional.ofNullable(System.getProperty(key))
                .or(() -> Optional.ofNullable(System.getenv(key)))
                .or(() -> JigPropertyHolder.getInstance().get(key));
    }

    List<Editor> editors;

    String toText(Node original) {
        Node node = original;
        for (Editor editor : editors) {
            node = editor.edit(node);
        }

        Map<String, String> map = new LinkedHashMap<>(node.attributeMap);
        map.replaceAll((key, value) -> {
            if (key.equals("htmlLabel")) return value;
            if (英数のみ.test(value)) return value;

            // "をエスケープして"で括る
            return String.format("\"%s\"", value.replace("\"", "\\\""));
        });
        if (map.containsKey("htmlLabel")) {
            String html = map.remove("htmlLabel");
            map.put("label", String.format("<%s>", html));
        }

        String attribute = map.entrySet().stream()
                .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(",", "[", "]"));
        return '"' + node.identifier + '"' + attribute + ';';
    }

    static class Editor {

        private Predicate<Node> predicate;
        private Function<Node, Node> editor;

        Editor(Predicate<Node> predicate, Function<Node, Node> editor) {
            this.predicate = predicate;
            this.editor = editor;
        }

        Node edit(Node node) {
            if (predicate.test(node)) {
                return editor.apply(node);
            }
            return node;
        }
    }
}
