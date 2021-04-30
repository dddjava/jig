package org.dddjava.jig.domain.model.jigdocument.stationery;

import org.dddjava.jig.domain.model.jigmodel.architecture.ArchitectureModule;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.jigmodel.categories.CategoryType;
import org.dddjava.jig.domain.model.parts.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.parts.declaration.type.TypeIdentifier;

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

    public static Node typeOf(TypeIdentifier identifier) {
        return new Node(identifier.fullQualifiedName());
    }

    public static Node packageOf(PackageIdentifier identifier) {
        return new Node(identifier.asText());
    }

    public static Node businessRuleNodeOf(BusinessRule businessRule) {
        return new Node(businessRule.typeIdentifier().fullQualifiedName())
                .label(businessRule.nodeLabel())
                .highlightColorIf(businessRule.markedCore());
    }

    public static Node architectureModuleOf(ArchitectureModule module) {
        return new Node(module.nodeLabel());
    }

    public static Node categoryNodeOf(CategoryType categoryType) {
        if (categoryType.markedCore()) {
            return new Node(categoryType.typeIdentifier().fullQualifiedName())
                    .highlightColor()
                    .label(categoryType.nodeLabel());
        } else if (categoryType.hasBehaviour()) {
            return new Node(categoryType.typeIdentifier().fullQualifiedName())
                    .weakColor()
                    .label(categoryType.nodeLabel());
        } else {
            return new Node(categoryType.typeIdentifier().fullQualifiedName())
                    .normalColor()
                    .label(categoryType.nodeLabel());
        }
    }

    /**
     * 主要でない
     */
    public Node other() {
        return fillColor("whitesmoke")
                .style("dashed");
    }

    public Node label(String value) {
        attribute.add("label=\"" + value.replace("\"", "\\\"") + "\"");
        return this;
    }

    Node fillColor(String value) {
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
        return style("solid").fillColor("black");
    }

    public Node lambda() {
        return shape("ellipse").fillColor("gray");
    }

    public Node handlerMethod() {
        return fillColor("greenyellow");
    }

    public Node html(String html) {
        attribute.add("label=<" + html + ">;");
        return this;
    }

    public Node normalColor() {
        return fillColor("lightgoldenrod");
    }

    public Node weakColor() {
        return fillColor("lemonchiffon");
    }


    public Node tooltip(String tooltip) {
        attribute.add("tooltip=\"" + tooltip + "\"");
        return this;
    }

    public Node screenNode() {
        // 画面
        return style("filled").fillColor("lightgray").shape("box");
    }

    public Node useCase() {
        return shape("ellipse");
    }

    public Node highlightColor() {
        return fillColor("greenyellow");
    }

    public Node moderately() {
        attribute.add("fontsize=8");
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

    public Node highlightColorIf(boolean markedCore) {
        return markedCore ? this.highlightColor() : this;
    }

    public Node big() {
        attribute.add("fontsize=30");
        return this;
    }

    public void warning() {
        attribute.add("color=red");
    }
}
