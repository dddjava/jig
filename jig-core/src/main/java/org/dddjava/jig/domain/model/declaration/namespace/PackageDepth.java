package org.dddjava.jig.domain.model.declaration.namespace;

import java.util.ArrayList;
import java.util.List;

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

    public boolean unlimited() {
        return value < 0;
    }

    public List<PackageDepth> surfaceList() {
        ArrayList<PackageDepth> list = new ArrayList<>();
        for (int i = value; i >= 0; i--) {
            list.add(new PackageDepth(i));
        }
        return list;
    }
}
