package org.dddjava.jig.presentation.view.graphvizj;

import java.util.StringJoiner;

public class RelationText {

    StringJoiner stringJoiner = new StringJoiner("\n");

    public void add(String from, String to) {
        String line = '"' + from + '"' + " -> " + '"' + to + '"' + ';';
        stringJoiner.add(line);
    }

    public String asText() {
        return stringJoiner.toString();
    }
}
