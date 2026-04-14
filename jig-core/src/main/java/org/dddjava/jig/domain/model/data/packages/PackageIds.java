package org.dddjava.jig.domain.model.data.packages;

import java.util.Set;

/**
 * パッケージ識別子一覧
 */
public record PackageIds(Set<PackageId> values) {

    public int size() {
        return values.size();
    }
}
