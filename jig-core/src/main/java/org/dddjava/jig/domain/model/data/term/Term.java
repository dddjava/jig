package org.dddjava.jig.domain.model.data.term;

import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

/**
 * 用語
 */
public record Term(TermIdentifier identifier, String title, String description, TermKind termKind) {

    public static Term fromPackage(PackageIdentifier packageIdentifier, String title, String description) {
        return new Term(new TermIdentifier(packageIdentifier.asText()), title, description, TermKind.パッケージ);
    }

    public static Term fromClass(TypeIdentifier typeIdentifier, String title, String description) {
        return new Term(new TermIdentifier(typeIdentifier.fullQualifiedName()), title, description, TermKind.クラス);
    }

    public static Term fromMethod(String identifier, String title, String description) {
        return new Term(new TermIdentifier(identifier), title, description, TermKind.メソッド);
    }

    @Override
    public String description() {
        return description.trim();
    }
}
