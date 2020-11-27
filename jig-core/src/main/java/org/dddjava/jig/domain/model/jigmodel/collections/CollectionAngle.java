package org.dddjava.jig.domain.model.jigmodel.collections;

import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigType;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelations;

/**
 * コレクションの切り口
 *
 * 以下を推測したい
 * ・ロジックがいろいろ書かれていそう --> そのクラスのロジックの書き方を重点レビュー
 * ・ロジックがほとんどなさそう --> そのクラスを使っているクラス側にロジックが書かれていないか、レビュー
 */
public class CollectionAngle {

    JigType jigType;
    ClassRelations classRelations;

    public CollectionAngle(JigType jigType, ClassRelations classRelations) {
        this.jigType = jigType;
        this.classRelations = classRelations;
    }

    public TypeIdentifier typeIdentifier() {
        return jigType.identifier();
    }

    public TypeIdentifiers userTypeIdentifiers() {
        return classRelations.collectTypeIdentifierWhichRelationTo(typeIdentifier());
    }

    public MethodDeclarations methods() {
        return jigType.instanceMember().instanceMethods().declarations();
    }

    public JigType jigType() {
        return jigType;
    }
}
