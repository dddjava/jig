package org.dddjava.jig.domain.model.implementation.analyzed.networks.class_;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.packages.PackageRelation;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.packages.PackageRelations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 型依存関係一覧
 */
public class ClassRelations {

    List<ClassRelation> list;

    public ClassRelations(TypeByteCodes typeByteCodes) {
        this.list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            TypeIdentifier form = typeByteCode.typeIdentifier();
            for (TypeIdentifier to : typeByteCode.useTypes().list()) {
                ClassRelation classRelation = new ClassRelation(form, to);
                if (classRelation.selfRelation()) continue;
                list.add(classRelation);
            }
        }
    }

    public TypeIdentifiers collectTypeIdentifierWhichRelationTo(TypeIdentifier typeIdentifier) {
        return list.stream()
                .filter(classRelation -> classRelation.toIs(typeIdentifier))
                .map(ClassRelation::from)
                .collect(TypeIdentifiers.collector())
                .normalize();
    }

    public PackageRelations packageDependencies() {
        List<PackageRelation> packageRelationList = list.stream()
                .map(ClassRelation::toPackageDependency)
                .filter(PackageRelation::notSelfRelation)
                .distinct()
                .collect(Collectors.toList());

        return new PackageRelations(packageRelationList);
    }

    public List<ClassRelation> list() {
        return list;
    }
}
