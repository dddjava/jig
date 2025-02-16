package org.dddjava.jig.domain.model.data.packages;

/**
 * パッケージの深さ
 */
public class PackageDepth {
    int value;

    public PackageDepth(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    public boolean just(PackageDepth other) {
        return this.value == other.value;
    }
}
