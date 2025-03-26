package org.dddjava.jig.domain.model.information.module;

import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.Collection;
import java.util.List;

public record JigPackage(PackageIdentifier packageIdentifier, Term term, Collection<JigType> jigTypes) {

    public JigPackage(PackageIdentifier packageIdentifier, Term term) {
        this(packageIdentifier, term, List.of());
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
}
