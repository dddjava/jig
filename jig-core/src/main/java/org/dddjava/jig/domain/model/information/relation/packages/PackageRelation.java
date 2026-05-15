package org.dddjava.jig.domain.model.information.relation.packages;

import org.dddjava.jig.domain.model.data.packages.PackageId;

/**
 * パッケージの関連
 *
 * @param deprecatedOnly この関連を構成するクラス関連がすべて、少なくとも片方の型がDeprecatedである場合にtrue
 */
public record PackageRelation(PackageId from, PackageId to, boolean deprecatedOnly) {
}
