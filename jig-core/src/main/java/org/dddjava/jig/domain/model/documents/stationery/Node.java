package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;

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

    public static Node typeOf(TypeId typeId) {
        return new Node(typeId.fullQualifiedName());
    }

    public static Node packageOf(PackageId identifier) {
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

    public Node url(PackageId packageId, JigDocument jigDocument) {
        String ref = jigDocument.fileName() + ".html";
        attributeMap.put("URL", "./" + ref + "#" + packageId.asText());
        return this;
    }

    public Node url(TypeId typeId, JigDocument jigDocument) {
        String ref = jigDocument.fileName() + ".html";
        attributeMap.put("URL", "./" + ref + "#" + typeId.fullQualifiedName());
        return this;
    }

    public Node big() {
        attributeMap.put("fontsize", "30");
        return this;
    }

    public void warning() {
        attributeMap.put("color", "red");
    }

    public boolean labelMatches(Predicate<String> predicate) {
        String label = attributeMap.get("label");
        return label != null && predicate.test(label);
    }

    public boolean identifierMatches(Predicate<String> predicate) {
        return predicate.test(identifier);
    }

    public void deprecated() {
        fillColor("gray");
    }
}
