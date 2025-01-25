package org.dddjava.jig.domain.model.documents.stationery;

import java.util.StringJoiner;
import java.util.stream.Stream;

public class Subgraph {
    StringJoiner stringJoiner;

    public Subgraph(String name) {
        stringJoiner = new StringJoiner("\n", "subgraph \"cluster_" + name + "\" {\n", "\n}");
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

    public Subgraph addNodes(Stream<Node> nodes) {
        // うけとったStreamの終端操作しちゃうのはどうなのよと思いつつ
        nodes.map(Node::asText).forEach(this::add);
        return this;
    }

    @Override
    public String toString() {
        return stringJoiner.toString();
    }
}
