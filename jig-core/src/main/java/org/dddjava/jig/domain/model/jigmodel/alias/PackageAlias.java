package org.dddjava.jig.domain.model.jigmodel.alias;

import org.dddjava.jig.domain.model.jigmodel.declaration.package_.PackageIdentifier;

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

    public static PackageAlias empty(PackageIdentifier packageIdentifier) {
        return new PackageAlias(packageIdentifier, Alias.empty());
    }

    public PackageIdentifier packageIdentifier() {
        return packageIdentifier;
    }

    public boolean exists() {
        return alias.exists();
    }

    public String asText() {
        return alias.toString();
    }
}
