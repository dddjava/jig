package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.implementation.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.implementation.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.implementation.declaration.type.TypeIdentifier;

import java.util.StringJoiner;

/**
 * ノードの表現
 */
public class Node {
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

    public Node label(String value) {
        attribute.add("label=\"" + value.replace("\"", "\\\"") + "\"");
        return this;
    }

    public Node color(String value) {
        attribute.add("color=\"" + value + "\"");
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

    Node notPublicMethod() {
        return style("solid").color("black");
    }

    Node lambda() {
        return shape("ellipse").color("gray");
    }

    Node handlerMethod() {
        return color("greenyellow");
    }
}
