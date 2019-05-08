package org.dddjava.jig.presentation.view.graphvizj;

import java.util.StringJoiner;

public class Subgraph {
    StringJoiner stringJoiner;

    public Subgraph(String name) {
        stringJoiner = new StringJoiner("\n", "subgraph cluster_" + name + "{", "}");
    }

    public Subgraph label(String label) {
        return add("label=" + label + ";");
    }

    Subgraph add(CharSequence charSequence) {
        stringJoiner.add(charSequence);
        return this;
    }

    @Override
    public String toString() {
        return stringJoiner.toString();
    }
}
