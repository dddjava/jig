package org.dddjava.jig.domain.model.implementation.networks.type;

import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.networks.packages.PackageRelation;
import org.dddjava.jig.domain.model.implementation.networks.packages.PackageRelations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 型依存関係一覧
 */
public class TypeRelations {

    List<TypeRelation> list;

    public TypeRelations(TypeByteCodes typeByteCodes) {
        this.list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            TypeIdentifier form = typeByteCode.typeIdentifier();
            for (TypeIdentifier to : typeByteCode.useTypes().list()) {
                list.add(new TypeRelation(form, to));
            }
        }
    }

    public TypeIdentifiers collectTypeIdentifierWhichRelationTo(TypeIdentifier typeIdentifier) {
        return list.stream()
                .filter(typeRelation -> typeRelation.toIs(typeIdentifier))
                .filter(TypeRelation::notSelfDependency)
                .map(TypeRelation::from)
                .collect(TypeIdentifiers.collector())
                .normalize();
    }

    public PackageRelations packageDependencies() {
        List<PackageRelation> packageRelationList = list.stream()
                .map(TypeRelation::toPackageDependency)
                .filter(PackageRelation::notSelfRelation)
                .distinct()
                .collect(Collectors.toList());

        return new PackageRelations(packageRelationList);
    }

    public List<TypeRelation> list() {
        return list;
    }
}
