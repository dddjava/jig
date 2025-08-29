package org.dddjava.jig.domain.model.data.packages;

/**
 * パッケージの深さ
 */
public record PackageDepth(int value) {

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    public boolean just(PackageDepth other) {
        return this.value == other.value;
    }
}
