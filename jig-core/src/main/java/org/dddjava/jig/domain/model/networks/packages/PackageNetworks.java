package org.dddjava.jig.domain.model.networks.packages;

import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class PackageNetworks {
    PackageNetwork origin;
    PackageDepth depth;

    public PackageNetworks(PackageNetwork origin, PackageDepth depth) {
        this.origin = origin;
        this.depth = depth;
    }

    public List<PackageNetwork> list() {
        PackageDepth maxDepth = origin.maxDepthWith(depth);

        return maxDepth.surfaceList().stream()
                .map(depth -> origin.applyDepth(depth))
                .filter(PackageNetwork::available)
                .collect(toList());
    }
}
