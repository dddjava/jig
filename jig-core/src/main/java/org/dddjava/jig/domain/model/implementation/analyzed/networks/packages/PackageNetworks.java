package org.dddjava.jig.domain.model.implementation.analyzed.networks.packages;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.package_.PackageDepth;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class PackageNetworks {
    PackageNetwork origin;

    public PackageNetworks(PackageNetwork origin) {
        this.origin = origin;
    }

    public List<PackageNetwork> list() {
        PackageDepth maxDepth = origin.maxDepth();

        return maxDepth.surfaceList().stream()
                .map(depth -> origin.applyDepth(depth))
                .filter(PackageNetwork::available)
                .collect(toList());
    }
}
