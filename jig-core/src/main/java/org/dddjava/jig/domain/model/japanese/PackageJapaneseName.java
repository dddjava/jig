package org.dddjava.jig.domain.model.japanese;

import org.dddjava.jig.domain.model.identifier.namespace.PackageIdentifier;

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
