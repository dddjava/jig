package org.dddjava.jig.domain.model.identifier.type;

import org.dddjava.jig.domain.model.identifier.namespace.PackageIdentifier;

import java.util.Objects;

/**
 * 型の識別子
 */
public class TypeIdentifier {

    String value;

    public TypeIdentifier(Class<?> clz) {
        this(clz.getName());
    }

    public TypeIdentifier(String value) {
        this.value = value.replace('/', '.');
    }

    public String fullQualifiedName() {
        return format(value -> value);
    }

    public String asSimpleText() {
        return hasPackage() ? format(value -> value.substring(value.lastIndexOf(".") + 1)) : value;
    }

    public String format(TypeIdentifierFormatter formatter) {
        return formatter.format(value);
    }

    private boolean hasPackage() {
        return value.contains(".");
    }

    public PackageIdentifier packageIdentifier() {
        if (!hasPackage()) {
            return PackageIdentifier.defaultPackage();
        }
        return new PackageIdentifier(value.substring(0, value.lastIndexOf(".")));
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

    @Override
    public String toString() {
        return "TypeIdentifier{" +
                "value='" + value + '\'' +
                '}';
    }
}
