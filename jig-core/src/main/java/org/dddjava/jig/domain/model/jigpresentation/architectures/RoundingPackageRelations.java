package org.dddjava.jig.domain.model.jigpresentation.architectures;

import org.dddjava.jig.domain.model.jigloaded.relation.packages.PackageRelation;
import org.dddjava.jig.domain.model.jigloaded.relation.packages.PackageRelations;

import java.util.List;

/**
 * アーキテクチャー単位に丸めたパッケージ関連
 */
public class RoundingPackageRelations {

    List<PackageRelation> list;

    public RoundingPackageRelations(List<PackageRelation> list) {
        this.list = list;
    }

    public boolean worthless() {
        return list.isEmpty();
    }

    public PackageRelations packageRelations() {
        return new PackageRelations(list);
    }
}
