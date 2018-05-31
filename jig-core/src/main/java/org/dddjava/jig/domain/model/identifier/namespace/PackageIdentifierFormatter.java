package org.dddjava.jig.domain.model.identifier.namespace;

/**
 * パッケージ識別子のフォーマッタ
 */
public interface PackageIdentifierFormatter {

    String format(String fullQualifiedName);
}
