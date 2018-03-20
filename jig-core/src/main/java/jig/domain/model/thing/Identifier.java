package jig.domain.model.thing;

import java.util.Objects;

public class Identifier {

    String value;

    public Identifier(Class<?> clz) {
        this(clz.getName());
    }

    public Identifier(String value) {
        this.value = value.replace('/', '.');
    }

    public String value() {
        return value;
    }

    public String asCompressText() {
        return value.replaceAll("(\\w)\\w+\\.", "$1.");
    }

    public String asSimpleText() {
        return value.replaceAll("([\\w]+\\.)*(\\w+)", "$2");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifier that = (Identifier) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public Identifier concat(Identifier other) {
        // カッコより手前のドット以降
        String substring = other.value.substring(other.value.lastIndexOf(".", other.value.lastIndexOf("(")));
        return new Identifier(value + substring);
    }
}
