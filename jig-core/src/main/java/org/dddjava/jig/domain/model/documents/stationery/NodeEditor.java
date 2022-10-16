package org.dddjava.jig.domain.model.documents.stationery;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        editors = List.of(
                // labelが * で始まったらgreenyellowにする
                new Editor(node -> node.labelMatches(label -> label.startsWith("*")), node -> node.fillColor("greenyellow"))
        );
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
