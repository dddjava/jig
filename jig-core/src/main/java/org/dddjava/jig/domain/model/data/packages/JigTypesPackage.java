package org.dddjava.jig.domain.model.data.packages;

import org.dddjava.jig.domain.model.data.classes.type.JigType;

import java.util.List;

/**
 * パッケージ単位のJigTypeのグループ
 */
public record JigTypesPackage(PackageIdentifier packageIdentifier, List<JigType> jigTypes) {
}
