package org.dddjava.jig.domain.model.jigdocumenter.stationery;

import java.util.StringJoiner;

public class Subgraph {
    StringJoiner stringJoiner;

    public Subgraph(String name) {
        stringJoiner = new StringJoiner("\n", "subgraph \"cluster_" + name + "\"{", "}");
    }

    public Subgraph label(String label) {
        return add("label=\"" + label + "\";");
    }

    public Subgraph borderWidth(int width) {
        return add("penwidth=" + width + ";");
    }

    public Subgraph color(String color) {
        return add("color=\"" + color + "\";");
    }

    public Subgraph fillColor(String color) {
        return add("style=filled;fillcolor=\"" + color + "\";");
    }

    public Subgraph add(CharSequence charSequence) {
        stringJoiner.add(charSequence);
        return this;
    }

    @Override
    public String toString() {
        return stringJoiner.toString();
    }
}
