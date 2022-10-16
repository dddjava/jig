package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Pattern;

/**
 * ノードの表現
 */
public class Node {

    public static final String DEFAULT = "node [shape=box,style=filled,fillcolor=lightgoldenrod];";

    String identifier;
    StringJoiner attribute = new StringJoiner(",", "[", "]");

    Map<String, String> attributeMap = new HashMap<>();

    public Node(String identifier) {
        this.identifier = identifier;
    }

    public static Node typeOf(TypeIdentifier identifier) {
        return new Node(identifier.fullQualifiedName());
    }

    public static Node packageOf(PackageIdentifier identifier) {
        return new Node(identifier.asText());
    }

    public Node as(NodeRole nodeRole) {
        return nodeRole.edit(this);
    }

    public Node label(String value) {
        attribute.add("label=\"" + value.replace("\"", "\\\"") + "\"");
        attributeMap.put("label", value);
        return this;
    }

    public Node html(String summary, String html) {
        attribute.add("label=<" + html + ">");
        attributeMap.put("label", summary);
        return this;
    }

    Node fillColor(String value) {
        attribute.add("fillcolor=\"" + value + "\"");
        return this;
    }

    public String asText() {
        // "hoge"[label="fuga",color="piyo"];
        return NodeEditor.INSTANCE.toText(this);
    }

    Node shape(String value) {
        attribute.add("shape=\"" + value + "\"");
        return this;
    }

    public Node tooltip(String tooltip) {
        attribute.add("tooltip=\"" + tooltip + "\"");
        return this;
    }

    public Node url(PackageIdentifier packageIdentifier, JigDocumentContext jigDocumentContext) {
        if (jigDocumentContext.linkPrefix().disabled()) {
            return this;
        }
        attribute.add("URL=\"" + jigDocumentContext.linkPrefix().textValue() + '/' +
                packageIdentifier.asText().replaceAll("\\.", "/") +
                "\"");
        return this;
    }

    public Node url(TypeIdentifier typeIdentifier, JigDocumentContext jigDocumentContext) {
        if (jigDocumentContext.linkPrefix().disabled()) {
            return this;
        }
        attribute.add("URL=\"" + jigDocumentContext.linkPrefix().textValue() + '/' +
                // TODO CodeSourceから解決できるようにしたい。
                typeIdentifier.fullQualifiedName().replaceAll("\\.", "/") + ".java" +
                "\"");
        return this;
    }

    public Node big() {
        attribute.add("fontsize=30");
        return this;
    }

    public void warning() {
        attribute.add("color=red");
    }

    public boolean labelMatches(Pattern pattern) {
        String label = attributeMap.get("label");
        return label != null && pattern.asMatchPredicate().test(label);
    }
}
