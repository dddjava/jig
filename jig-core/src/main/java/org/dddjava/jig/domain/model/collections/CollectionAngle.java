package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.model.implementation.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.networks.type.TypeRelations;

/**
 * コレクションの切り口
 *
 * 以下を推測したい
 * ・ロジックがいろいろ書かれていそう --> そのクラスのロジックの書き方を重点レビュー
 * ・ロジックがほとんどなさそう --> そのクラスを使っているクラス側にロジックが書かれていないか、レビュー
 */
public class CollectionAngle {

    TypeIdentifier typeIdentifier;
    MethodDeclarations methods;
    TypeIdentifiers userTypeIdentifiers;

    public CollectionAngle(CollectionType collectionType, TypeRelations allTypeRelations) {
        this.typeIdentifier = collectionType.typeIdentifier();
        this.methods = collectionType.methods();
        this.userTypeIdentifiers = allTypeRelations.collectTypeIdentifierWhichRelationTo(typeIdentifier);
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public TypeIdentifiers userTypeIdentifiers() {
        return userTypeIdentifiers;
    }

    public MethodDeclarations methods() {
        return methods;
    }
}
