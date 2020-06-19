package org.dddjava.jig.domain.model.jigdocument.stationery;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

import java.util.StringJoiner;

/**
 * ノードの表現
 */
public class Node {

    public static final String DEFAULT = "node [shape=box,style=filled,fillcolor=lightgoldenrod];";

    String identifier;
    StringJoiner attribute = new StringJoiner(",", "[", "]");

    public Node(String identifier) {
        this.identifier = identifier;
    }

    public static Node controllerNodeOf(TypeIdentifier identifier) {
        return new Node(identifier.fullQualifiedName());
    }

    /**
     * 主要でない
     */
    public Node other() {
        return color("whitesmoke")
                .style("dashed");
    }

    public Node label(String value) {
        attribute.add("label=\"" + value.replace("\"", "\\\"") + "\"");
        return this;
    }

    Node color(String value) {
        attribute.add("fillcolor=\"" + value + "\"");
        return this;
    }

    public String asText() {
        // "hoge"[label="fuga",color="piyo"];
        return '"' + identifier + '"' + attribute + ';';
    }

    public Node style(String value) {
        attribute.add("style=\"" + value + "\"");
        return this;
    }

    public Node shape(String value) {
        attribute.add("shape=\"" + value + "\"");
        return this;
    }

    public Node notPublicMethod() {
        return style("solid").color("black");
    }

    public Node lambda() {
        return shape("ellipse").color("gray");
    }

    public Node handlerMethod() {
        return color("greenyellow");
    }

    public Node html(String html) {
        attribute.add("label=<" + html + ">;");
        return this;
    }

    public Node normalColor() {
        return color("lightgoldenrod");
    }

    public Node tooltip(String tooltip) {
        attribute.add("tooltip=\"" + tooltip + "\"");
        return this;
    }

    public Node screenNode() {
        // 画面
        return style("filled").color("lightgray").shape("box");
    }

    public Node useCase() {
        return shape("ellipse");
    }

    public Node highlightColor() {
        return color("greenyellow");
    }

    public Node moderately() {
        attribute.add("fontsize=8");
        return this;
    }
}
