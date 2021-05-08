package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;

/**
 * パッケージ識別子のフォーマッタ
 */
public interface PackageIdentifierFormatter {

    String format(String fullQualifiedName);

    default String format(PackageIdentifier packageIdentifier) {
        return format(packageIdentifier.asText());
    }
}
