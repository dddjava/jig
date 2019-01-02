package org.dddjava.jig.domain.model.implementation.declaration.namespace;

/**
 * パッケージ識別子のフォーマッタ
 */
public interface PackageIdentifierFormatter {

    String format(String fullQualifiedName);
}
