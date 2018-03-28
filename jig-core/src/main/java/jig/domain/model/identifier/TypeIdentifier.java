package jig.domain.model.identifier;

import java.util.Objects;

public class TypeIdentifier {

    String value;

    public TypeIdentifier(Class<?> clz) {
        this(clz.getName());
    }

    public TypeIdentifier(String value) {
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
        TypeIdentifier that = (TypeIdentifier) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public PackageIdentifier toPackage() {
        return new PackageIdentifier(this);

    }
}
