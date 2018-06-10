package org.dddjava.jig.domain.model.networks;

import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;
import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifiers;

/**
 * パッケージの関連
 */
public class PackageNetwork {

    PackageIdentifiers packageIdentifiers;
    PackageDependencies packageDependencies;

    public PackageNetwork(PackageIdentifiers packageIdentifiers, PackageDependencies packageDependencies) {
        this.packageIdentifiers = packageIdentifiers;
        this.packageDependencies = packageDependencies;
    }

    public PackageIdentifiers allPackages() {
        return packageIdentifiers;
    }

    public PackageDependencies packageDependencies() {
        return packageDependencies;
    }

    public PackageNetwork applyDepth(PackageDepth depth) {
        if (depth.unlimited()) return this;
        return new PackageNetwork(
                packageIdentifiers.applyDepth(depth),
                packageDependencies.applyDepth(depth));
    }
}
