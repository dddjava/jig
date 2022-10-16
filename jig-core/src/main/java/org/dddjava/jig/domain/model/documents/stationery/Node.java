package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.models.domains.categories.CategoryType;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;

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
        return NodeType.モブ.edit(this);
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
        return NodeType.スポットライト.edit(this);
    }

    public Node html(String html) {
        attribute.add("label=<" + html + ">;");
        return this;
    }

    public Node normalColor() {
        return NodeType.主役.edit(this);
    }

    public Node weakColor() {
        return NodeType.脇役.edit(this);
    }


    public Node tooltip(String tooltip) {
        attribute.add("tooltip=\"" + tooltip + "\"");
        return this;
    }

    public Node screenNode() {
        // 画面
        return NodeType.モブ.edit(this);
        // TODO 色以外の指定が必要かを確認
        // style("filled").fillColor("lightgray").shape("box");
    }

    public Node useCase() {
        return shape("ellipse");
    }

    public Node highlightColor() {
        return NodeType.スポットライト.edit(this);
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
