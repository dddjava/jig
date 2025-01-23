package org.dddjava.jig.domain.model.data.jigobject.package_;

import org.dddjava.jig.domain.model.data.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;

import java.util.List;

/**
 * パッケージ単位のJigTypeのグループ
 */
public record JigTypesPackage(PackageIdentifier packageIdentifier, List<JigType> jigTypes) {
}
