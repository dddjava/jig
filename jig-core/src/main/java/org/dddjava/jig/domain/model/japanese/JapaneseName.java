package org.dddjava.jig.domain.model.japanese;

public class JapaneseName {

    final String value;

    public JapaneseName(String value) {
        this.value = value;
    }

    public String summarySentence() {
        if (value.contains("\n") || value.contains("。")) {
            int end = Math.min(value.indexOf("\n"), value.indexOf("。"));
            return value.substring(0, end);
        }
        return value;
    }

    public String value() {
        return value;
    }
}
