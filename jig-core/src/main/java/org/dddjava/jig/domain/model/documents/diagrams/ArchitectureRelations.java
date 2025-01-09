package org.dddjava.jig.domain.model.documents.diagrams;

import org.dddjava.jig.domain.model.data.classes.type.ClassRelation;
import org.dddjava.jig.domain.model.data.classes.type.ClassRelations;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageRelation;
import org.dddjava.jig.domain.model.data.packages.PackageRelations;
import org.dddjava.jig.domain.model.knowledge.architecture.PackageBasedArchitecture;

import java.util.ArrayList;
import java.util.List;

/**
 * アーキテクチャー単位に丸めたパッケージ関連
 */
public class ArchitectureRelations {

    List<PackageRelation> list;

    public ArchitectureRelations(List<PackageRelation> list) {
        this.list = list;
    }

    public static ArchitectureRelations from(PackageBasedArchitecture packageBasedArchitecture, ClassRelations classRelations) {
        ArrayList<PackageRelation> list = new ArrayList<>();
        for (ClassRelation classRelation : classRelations.list()) {
            TypeIdentifier from = classRelation.from();
            TypeIdentifier to = classRelation.to();

            PackageIdentifier fromPackage = packageBasedArchitecture.packageIdentifier(from);

            if (to.isJavaLanguageType()) {
                // 興味のない関連
                continue;
            }
            PackageIdentifier toPackage = packageBasedArchitecture.packageIdentifier(to);

            if (fromPackage.equals(toPackage)) {
                // 自己参照
                continue;
            }
            PackageRelation e = new PackageRelation(fromPackage, toPackage);
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
