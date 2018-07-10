package org.dddjava.jig.domain.model.networks.packages;

import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;

import java.util.Collections;
import java.util.List;

public class PackageNetworks {
    PackageNetwork origin;
    PackageDepth depth;

    public PackageNetworks(PackageNetwork origin, PackageDepth depth) {
        this.origin = origin;
        this.depth = depth;
    }

    public List<PackageNetwork> list() {
        if (depth.unlimited()) return Collections.singletonList(origin);
        return Collections.singletonList(origin.applyDepth(depth));
    }
}
