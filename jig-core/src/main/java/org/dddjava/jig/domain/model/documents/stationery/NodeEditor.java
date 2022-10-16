package org.dddjava.jig.domain.model.documents.stationery;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * ノードの最終的な編集者
 */
public class NodeEditor {
    static final NodeEditor INSTANCE = new NodeEditor();

    private NodeEditor() {
        editors = List.of(
                // labelが * で始まったらgreenyellowにする
                new Editor(node -> node.labelMatches(Pattern.compile("^\\*.+")), node -> node.fillColor("greenyellow"))
        );
    }

    List<Editor> editors;

    String toText(Node original) {
        Node node = original;
        for (Editor editor : editors) {
            node = editor.edit(node);
        }
        return '"' + node.identifier + '"' + node.attribute + ';';
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
