package org.dddjava.jig.domain.model.knowledge.module;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.Collection;
import java.util.List;

/**
 * パッケージ単位のJigTypeのグループ
 *
 * JigPackageWithJigTypesとの違いはパッケージの用語を持つこと
 */
public record JigPackage(PackageId packageId, Term term, Collection<JigType> jigTypes) {

    public JigPackage(PackageId packageId, Term term) {
        this(packageId, term, List.of());
    }

    public String fqn() {
        return packageId.asText();
    }

    public String label() {
        return term.title();
    }

    public int numberOfClasses() {
        return jigTypes.size();
    }
}
