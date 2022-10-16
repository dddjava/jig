package org.dddjava.jig.domain.model.documents.stationery;

import java.util.function.Function;

/**
 * Nodeの種類
 *
 * 色と文字サイズで表現する。
 * 何にどのタイプを使用するかは各ダイアグラムで決める。
 */
public enum NodeType {
    スポットライト(node -> node.fillColor("greenyellow")),
    主役(node -> node.fillColor("lightgoldenrod")),
    脇役(node -> node.fillColor("lemonchiffon")),
    モブ(node -> node.fillColor("lightgray"))
    ;
    final Function<Node, Node> editor;

    NodeType(Function<Node, Node> editor) {
        this.editor = editor;
    }

    Node edit(Node node) {
        return editor.apply(node);
    }
}
