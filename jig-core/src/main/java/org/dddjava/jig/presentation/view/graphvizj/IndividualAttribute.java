package org.dddjava.jig.presentation.view.graphvizj;

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
}
