package org.dddjava.jig.domain.model.jigmodel.lowmodel.alias;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;

/**
 * パッケージ別名
 */
public class PackageAlias {
    PackageIdentifier packageIdentifier;
    DocumentationComment documentationComment;

    public PackageAlias(PackageIdentifier packageIdentifier, DocumentationComment documentationComment) {
        this.packageIdentifier = packageIdentifier;
        this.documentationComment = documentationComment;
    }

    public static PackageAlias empty(PackageIdentifier packageIdentifier) {
        return new PackageAlias(packageIdentifier, DocumentationComment.empty());
    }

    public PackageIdentifier packageIdentifier() {
        return packageIdentifier;
    }

    public boolean exists() {
        return documentationComment.exists();
    }

    public String asText() {
        return documentationComment.summaryText();
    }

    public String summaryOrSimpleName() {
        if (documentationComment.exists()) {
            return documentationComment.summaryText();
        }
        return packageIdentifier.simpleName();
    }

    public String descriptionText() {
        return documentationComment.fullText();
    }
}
