package org.dddjava.jig.domain.model.implementation.analyzed.alias;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.package_.PackageIdentifier;

/**
 * パッケージ別名
 */
public class PackageAlias {
    PackageIdentifier packageIdentifier;
    Alias alias;

    public PackageAlias(PackageIdentifier packageIdentifier, Alias alias) {
        this.packageIdentifier = packageIdentifier;
        this.alias = alias;
    }

    public PackageIdentifier packageIdentifier() {
        return packageIdentifier;
    }

    public Alias japaneseName() {
        return alias;
    }

    public boolean exists() {
        return alias.exists();
    }
}
