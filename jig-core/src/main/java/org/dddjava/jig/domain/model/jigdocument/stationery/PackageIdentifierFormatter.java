package org.dddjava.jig.domain.model.jigdocument.stationery;

import org.dddjava.jig.domain.model.parts.package_.PackageIdentifier;

/**
 * パッケージ識別子のフォーマッタ
 */
public interface PackageIdentifierFormatter {

    String format(String fullQualifiedName);

    default String format(PackageIdentifier packageIdentifier) {
        return format(packageIdentifier.asText());
    }
}
