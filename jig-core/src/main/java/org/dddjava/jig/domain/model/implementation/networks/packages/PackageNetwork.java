package org.dddjava.jig.domain.model.implementation.networks.packages;

import org.dddjava.jig.domain.model.implementation.declaration.namespace.PackageDepth;
import org.dddjava.jig.domain.model.implementation.declaration.namespace.PackageIdentifiers;

/**
 * パッケージの関連
 */
public class PackageNetwork {

    PackageIdentifiers packageIdentifiers;
    PackageRelations packageRelations;
    PackageDepth appliedDepth;

    public PackageNetwork(PackageIdentifiers packageIdentifiers, PackageRelations packageRelations) {
        this(packageIdentifiers, packageRelations, new PackageDepth(-1));
    }

    private PackageNetwork(PackageIdentifiers packageIdentifiers, PackageRelations packageRelations, PackageDepth appliedDepth) {
        this.packageIdentifiers = packageIdentifiers;
        this.packageRelations = packageRelations;
        this.appliedDepth = appliedDepth;
    }

    public PackageIdentifiers allPackages() {
        return packageIdentifiers;
    }

    public PackageRelations packageDependencies() {
        return packageRelations.filterBothMatch(packageIdentifiers);
    }

    public PackageNetwork applyDepth(PackageDepth depth) {
        return new PackageNetwork(
                packageIdentifiers.applyDepth(depth),
                packageRelations.applyDepth(depth),
                depth
        );
    }

    public PackageDepth appliedDepth() {
        return appliedDepth;
    }

    public boolean available() {
        return packageDependencies().available();
    }

    public PackageDepth maxDepthWith(PackageDepth depth) {
        return depth.unlimited() ? packageIdentifiers.maxDepth() : depth;
    }
}
