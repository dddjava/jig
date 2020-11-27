package org.dddjava.jig.domain.model.jigmodel.collections;

import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigType;
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

    public TypeIdentifiers userTypeIdentifiers() {
        return classRelations.collectTypeIdentifierWhichRelationTo(jigType().identifier());
    }

    public JigType jigType() {
        return jigType;
    }
}
