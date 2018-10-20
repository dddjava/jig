package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.networks.type.TypeDependencies;

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

    public CollectionAngle(CollectionType collectionType, TypeDependencies allTypeDependencies) {
        this.typeIdentifier = collectionType.typeIdentifier();
        this.methods = collectionType.methods();
        this.userTypeIdentifiers = allTypeDependencies.stream()
                .filterTo(typeIdentifier)
                .removeSelf()
                .fromTypeIdentifiers();
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
