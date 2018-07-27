package org.dddjava.jig.domain.model.networks.packages;

import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;
import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifiers;

/**
 * パッケージの関連
 */
public class PackageNetwork {

    PackageIdentifiers packageIdentifiers;
    PackageDependencies packageDependencies;
    PackageDepth appliedDepth;

    public PackageNetwork(PackageIdentifiers packageIdentifiers, PackageDependencies packageDependencies) {
        this(packageIdentifiers, packageDependencies, new PackageDepth(-1));
    }

    private PackageNetwork(PackageIdentifiers packageIdentifiers, PackageDependencies packageDependencies, PackageDepth appliedDepth) {
        this.packageIdentifiers = packageIdentifiers;
        this.packageDependencies = packageDependencies;
        this.appliedDepth = appliedDepth;
    }

    public PackageIdentifiers allPackages() {
        return packageIdentifiers;
    }

    public PackageDependencies packageDependencies() {
        return packageDependencies.filterBothMatch(packageIdentifiers);
    }

    public PackageNetwork applyDepth(PackageDepth depth) {
        return new PackageNetwork(
                packageIdentifiers.applyDepth(depth),
                packageDependencies.applyDepth(depth),
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
