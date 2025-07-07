package org.dddjava.jig.domain.model.data.terms;

/**
 * 用語の識別子
 */
public record TermId(String value) {

    public String asText() {
        return value;
    }

    String simpleText() {
        int methodStart = value.indexOf('#');
        if (methodStart == -1) {
            int lastDot = value.lastIndexOf('.');

            return lastDot != -1 ? value.substring(lastDot + 1) : value;
        }

        int argStart = value.indexOf('(');
        if (argStart == -1) {
            // ないはず
            return value;
        }
        return value.substring(methodStart + 1, argStart);
    }
}
