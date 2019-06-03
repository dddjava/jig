package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;

import java.util.StringJoiner;

/**
 * ノードの表現
 */
public class Node {

    static final String DEFAULT = "node [shape=box,style=filled,fillcolor=lightgoldenrod];";

    String identifier;
    StringJoiner attribute = new StringJoiner(",", "[", "]");

    public Node(String identifier) {
        this.identifier = identifier;
    }

    public static Node of(PackageIdentifier identifier) {
        return new Node(identifier.asText());
    }

    public static Node of(TypeIdentifier identifier) {
        return new Node(identifier.fullQualifiedName());
    }

    public static Node of(MethodDeclaration identifier) {
        return new Node(identifier.asFullNameText());
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

    private Node color(String value) {
        attribute.add("fillcolor=\"" + value + "\"");
        return this;
    }

    public String asText() {
        // "hoge"[label="fuga",color="piyo"];
        return '"' + identifier + '"' + attribute + ';' ;
    }

    public Node style(String value) {
        attribute.add("style=\"" + value + "\"");
        return this;
    }

    public Node shape(String value) {
        attribute.add("shape=\"" + value + "\"");
        return this;
    }

    Node notPublicMethod() {
        return style("solid").color("black");
    }

    Node lambda() {
        return shape("ellipse").color("gray");
    }

    Node handlerMethod() {
        return color("greenyellow");
    }

    Node notEnum() {
        return color("lightyellow");
    }

    public Node html(String html) {
        attribute.add("label=<" + html + ">;");
        return this;
    }
}
