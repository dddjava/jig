package org.dddjava.jig.domain.model.jigmodel.jigtype.package_;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.PackageAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;

public class JigPackage {
    PackageIdentifier packageIdentifier;
    PackageAlias packageAlias;

    public JigPackage(PackageIdentifier packageIdentifier, PackageAlias packageAlias) {
        this.packageIdentifier = packageIdentifier;
        this.packageAlias = packageAlias;
    }

    public String simpleName() {
        return packageIdentifier.simpleName();
    }

    /**
     * FullQualifiedName
     */
    public String fqn() {
        return packageIdentifier.asText();
    }

    public String label() {
        return packageAlias.summaryOrSimpleName();
    }

    public String descriptionText() {
        return packageAlias.descriptionText();
    }
}
