package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelation;
import org.dddjava.jig.domain.model.information.relation.packages.PackageRelations;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationship;
import org.dddjava.jig.domain.model.knowledge.architecture.PackageBasedArchitecture;

import java.util.ArrayList;
import java.util.List;

/**
 * アーキテクチャー単位に丸めたパッケージ関連
 */
public class ArchitectureRelations {

    List<PackageRelation> list;

    private ArchitectureRelations(List<PackageRelation> list) {
        this.list = list;
    }

    public static ArchitectureRelations from(PackageBasedArchitecture packageBasedArchitecture) {
        ArrayList<PackageRelation> list = new ArrayList<>();
        for (TypeRelationship typeRelationship : packageBasedArchitecture.classRelations().list()) {
            TypeIdentifier from = typeRelationship.from();
            TypeIdentifier to = typeRelationship.to();

            PackageId fromPackage = packageBasedArchitecture.packageIdentifier(from);

            if (to.isJavaLanguageType()) {
                // 興味のない関連
                continue;
            }
            PackageId toPackage = packageBasedArchitecture.packageIdentifier(to);

            if (fromPackage.equals(toPackage)) {
                // 自己参照
                continue;
            }
            PackageRelation e = PackageRelation.from(fromPackage, toPackage);
            if (!list.contains(e)) {
                list.add(e);
            }
        }

        return new ArchitectureRelations(list);
    }

    public boolean worthless() {
        return list.isEmpty();
    }

    public PackageRelations packageRelations() {
        return new PackageRelations(list);
    }
}
