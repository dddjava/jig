package org.dddjava.jig.domain.model.declaration.package_;

/**
 * パッケージ識別子のフォーマッタ
 */
public interface PackageIdentifierFormatter {

    String format(String fullQualifiedName);
}
