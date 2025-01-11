package org.dddjava.jig.domain.model.information.domains.businessrules;

import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;

import java.util.List;

/**
 * ビジネスルールのグループ（パッケージ）
 */
public record BusinessRulePackage(PackageIdentifier packageIdentifier, List<JigType> jigTypes) {

    public PackageIdentifier packageIdentifier() {
        return packageIdentifier;
    }

    public List<JigType> businessRules() {
        return jigTypes;
    }
}
