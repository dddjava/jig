package org.dddjava.jig.domain.model.implementation.japanese;

import org.dddjava.jig.domain.model.implementation.declaration.namespace.PackageIdentifier;

/**
 * パッケージ和名
 */
public class PackageJapaneseName {
    PackageIdentifier packageIdentifier;
    JapaneseName japaneseName;

    public PackageJapaneseName(PackageIdentifier packageIdentifier, JapaneseName japaneseName) {
        this.packageIdentifier = packageIdentifier;
        this.japaneseName = japaneseName;
    }

    public PackageIdentifier packageIdentifier() {
        return packageIdentifier;
    }

    public JapaneseName japaneseName() {
        return japaneseName;
    }

    public boolean exists() {
        return japaneseName.exists();
    }
}
