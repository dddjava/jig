package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.type.TypeRelations;

/**
 * コレクションの切り口
 *
 * 以下を推測したい
 * ・ロジックがいろいろ書かれていそう --> そのクラスのロジックの書き方を重点レビュー
 * ・ロジックがほとんどなさそう --> そのクラスを使っているクラス側にロジックが書かれていないか、レビュー
 */
public class CollectionAngle {

    CollectionType collectionType;
    TypeRelations typeRelations;

    public CollectionAngle(CollectionType collectionType, TypeRelations typeRelations) {
        this.collectionType = collectionType;
        this.typeRelations = typeRelations;
    }

    public TypeIdentifier typeIdentifier() {
        return collectionType.typeIdentifier();
    }

    public TypeIdentifiers userTypeIdentifiers() {
        return typeRelations.collectTypeIdentifierWhichRelationTo(typeIdentifier());
    }

    public MethodDeclarations methods() {
        return collectionType.methods();
    }
}
