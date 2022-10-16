package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * ノードの表現
 */
public class Node {

    public static final String DEFAULT = "node [shape=box,style=filled,fillcolor=lightgoldenrod];";

    String identifier;

    Map<String, String> attributeMap = new LinkedHashMap<>();

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
        attributeMap.put("label", value);
        return this;
    }

    public Node html(String summary, String html) {
        attributeMap.put("label", summary);
        attributeMap.put("htmlLabel", html);
        return this;
    }

    Node fillColor(String value) {
        attributeMap.put("fillcolor", value);
        return this;
    }

    public String asText() {
        // "hoge"[label="fuga",color="piyo"];
        return NodeEditor.INSTANCE.toText(this);
    }

    Node shape(String value) {
        attributeMap.put("shape", value);
        return this;
    }

    public Node tooltip(String value) {
        attributeMap.put("tooltip", value);
        return this;
    }

    public Node url(PackageIdentifier packageIdentifier, JigDocumentContext jigDocumentContext) {
        if (jigDocumentContext.linkPrefix().disabled()) {
            return this;
        }
        attributeMap.put("URL",
                jigDocumentContext.linkPrefix().textValue() + '/' + packageIdentifier.asText().replaceAll("\\.", "/"));
        return this;
    }

    public Node url(TypeIdentifier typeIdentifier, JigDocumentContext jigDocumentContext) {
        if (jigDocumentContext.linkPrefix().disabled()) {
            return this;
        }
        attributeMap.put("URL",
                jigDocumentContext.linkPrefix().textValue() + '/' +
                        // TODO CodeSourceから解決できるようにしたい。
                        typeIdentifier.fullQualifiedName().replaceAll("\\.", "/") + ".java");
        return this;
    }

    public Node big() {
        attributeMap.put("fontsize", "30");
        return this;
    }

    public void warning() {
        attributeMap.put("color", "red");
    }

    public boolean labelMatches(Predicate<String> stringPredicate) {
        String label = attributeMap.get("label");
        return label != null && stringPredicate.test(label);
    }
}
