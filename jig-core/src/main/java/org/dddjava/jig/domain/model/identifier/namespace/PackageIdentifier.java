package org.dddjava.jig.domain.model.identifier.namespace;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * パッケージ識別子
 */
public class PackageIdentifier {

    String value;

    public PackageIdentifier(String value) {
        this.value = value;
    }

    public PackageIdentifier applyDepth(PackageDepth packageDepth) {
        String[] split = value.split("\\.");
        if (split.length < packageDepth.value()) return this;

        StringJoiner sj = new StringJoiner(".");
        for (int i = 0; i < packageDepth.value(); i++) {
            sj.add(split[i]);
        }
        return new PackageIdentifier(sj.toString());
    }

    public PackageDepth depth() {
        return new PackageDepth(value.split("\\.").length);
    }

    public String format(PackageIdentifierFormatter formatter) {
        return formatter.format(value);
    }

    public static PackageIdentifier defaultPackage() {
        return new PackageIdentifier("(default)");
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

    public String asText() {
        return value;
    }
}
