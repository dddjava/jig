package org.dddjava.jig.presentation.view.graphvizj;

import java.util.StringJoiner;

/**
 * 関連
 */
public class RelationText {

    StringJoiner stringJoiner = new StringJoiner("\n");

    public void add(String from, String to) {
        // "hoge" -> "fuga";
        String line = '"' + from + '"' + " -> " + '"' + to + '"' + ';';
        stringJoiner.add(line);
    }

    public String asText() {
        return stringJoiner.toString();
    }
}
