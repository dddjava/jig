package org.dddjava.jig.domain.model.parts.term;

import org.dddjava.jig.domain.model.parts.classes.method.MethodIdentifier;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;

/**
 * 用語
 */
public class Term {

    final TermIdentifier identifier;
    final String title;
    final String description;
    final TermKind termKind;

    public Term(TermIdentifier identifier, String title, String description, TermKind termKind) {
        this.identifier = identifier;
        this.title = title;
        this.description = description;
        this.termKind = termKind;
    }

    public static Term fromPackage(PackageIdentifier packageIdentifier, String title, String description) {
        return new Term(new TermIdentifier(packageIdentifier.asText()), title, description, TermKind.パッケージ);
    }

    public static Term fromClass(TypeIdentifier typeIdentifier, String title, String description) {
        return new Term(new TermIdentifier(typeIdentifier.fullQualifiedName()), title, description, TermKind.クラス);
    }

    public static Term fromMethod(MethodIdentifier methodIdentifier, String title, String description) {
        return new Term(new TermIdentifier(methodIdentifier.asText()), title, description, TermKind.メソッド);
    }

    public TermIdentifier identifier() {
        return identifier;
    }

    public String title() {
        return title;
    }

    public TermKind termKind() {
        return termKind;
    }

    public String description() {
        return description.trim();
    }
}
