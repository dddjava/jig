package org.dddjava.jig.domain.model.information.domains.businessrules;

import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;

import java.util.List;

/**
 * パッケージ単位のJigTypeのグループ
 */
public record PackageJigTypes(PackageIdentifier packageIdentifier, List<JigType> jigTypes) {
}
