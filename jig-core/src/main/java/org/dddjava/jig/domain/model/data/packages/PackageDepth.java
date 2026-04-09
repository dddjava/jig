package org.dddjava.jig.domain.model.data.packages;

/**
 * パッケージの深さ
 */
public record PackageDepth(int value) {

    @Override
    public String toString() {
        return Integer.toString(value);
    }

}
