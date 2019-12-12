package org.dddjava.jig.domain.model.jigloaded.alias;

/**
 * 別名
 */
public class Alias {

    String value;

    Alias(String value) {
        this.value = value;
    }

    public static Alias empty() {
        return new Alias("");
    }

    @Override
    public String toString() {
        return value;
    }

    public boolean exists() {
        return value.length() > 0;
    }
}
