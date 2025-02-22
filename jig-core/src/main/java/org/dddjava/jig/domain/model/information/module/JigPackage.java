package org.dddjava.jig.domain.model.information.module;

import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.term.Term;

public class JigPackage {
    PackageIdentifier packageIdentifier;
    private final Term term;

    public JigPackage(PackageIdentifier packageIdentifier, Term term) {
        this.packageIdentifier = packageIdentifier;
        this.term = term;
    }

    public PackageIdentifier packageIdentifier() {
        return packageIdentifier;
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
        return term.title();
    }

    public Term term() {
        return term;
    }
}
