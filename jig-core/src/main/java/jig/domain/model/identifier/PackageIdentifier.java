package jig.domain.model.identifier;

import jig.domain.model.relation.dependency.Depth;

import java.util.Objects;
import java.util.StringJoiner;

public class PackageIdentifier {

    String value;

    public PackageIdentifier(String value) {
        this.value = value;
    }

    public PackageIdentifier(TypeIdentifier typeIdentifier) {
        this(typeIdentifier.value.substring(0, typeIdentifier.value.lastIndexOf(".")));
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackageIdentifier that = (PackageIdentifier) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public PackageIdentifier applyDepth(Depth depth) {
        String[] split = value.split("\\.");
        if (split.length < depth.value()) return this;

        StringJoiner sj = new StringJoiner(".");
        for (int i = 0; i < depth.value(); i++) {
            sj.add(split[i]);
        }
        return new PackageIdentifier(sj.toString());
    }
}
