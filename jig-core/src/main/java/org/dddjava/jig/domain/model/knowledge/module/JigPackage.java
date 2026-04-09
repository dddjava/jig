package org.dddjava.jig.domain.model.knowledge.module;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.Collection;

/**
 * パッケージ単位のJigTypeのグループ
 *
 * JigPackageWithJigTypesとの違いはパッケージの用語を持つこと
 */
public record JigPackage(PackageId packageId, Collection<JigType> jigTypes) {

    public String fqn() {
        return packageId.asText();
    }

    public int numberOfClasses() {
        return jigTypes.size();
    }
}
