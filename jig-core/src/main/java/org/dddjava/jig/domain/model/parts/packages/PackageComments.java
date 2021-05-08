package org.dddjava.jig.domain.model.parts.packages;

import java.util.List;

/**
 * パッケージ名一覧
 */
public class PackageComments {
    List<PackageComment> list;

    public PackageComments(List<PackageComment> list) {
        this.list = list;
    }

    public List<PackageComment> list() {
        return list;
    }
}
