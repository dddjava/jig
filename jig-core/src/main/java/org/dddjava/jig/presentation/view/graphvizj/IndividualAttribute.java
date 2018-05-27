package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.identifier.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;

import java.util.StringJoiner;

/**
 * 個別の属性を設定する
 */
public class IndividualAttribute {
    String identifier;
    StringJoiner attribute = new StringJoiner(",", "[", "]");

    public IndividualAttribute(String identifier) {
        this.identifier = identifier;
    }

    public static IndividualAttribute of(PackageIdentifier identifier) {
        return new IndividualAttribute(identifier.asText());
    }

    public static IndividualAttribute of(TypeIdentifier identifier) {
        return new IndividualAttribute(identifier.fullQualifiedName());
    }

    public static IndividualAttribute of(MethodDeclaration identifier) {
        return new IndividualAttribute(identifier.asFullText());
    }

    public IndividualAttribute label(String value) {
        attribute.add("label=\"" + value + "\"");
        return this;
    }

    public IndividualAttribute color(String value) {
        attribute.add("color=\"" + value + "\"");
        return this;
    }

    public String asText() {
        // "hoge"[label="fuga",color="piyo"];
        return '"' + identifier + '"' + attribute + ';';
    }

    public IndividualAttribute style(String value) {
        attribute.add("style=\"" + value + "\"");
        return this;
    }

    public IndividualAttribute shape(String value) {
        attribute.add("shape=\"" + value + "\"");
        return this;
    }
}
