package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.TypeKind;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

/**
 * 区分
 */
public class CategoryType {
    JigType jigType;
    JigInstanceMember jigInstanceMember;

    public CategoryType(JigType jigType, JigInstanceMember jigInstanceMember) {
        this.jigType = jigType;
        this.jigInstanceMember = jigInstanceMember;
    }

    public boolean hasParameter() {
        return jigInstanceMember.hasField();
    }

    public boolean hasBehaviour() {
        // インスタンスメソッドがあるものを振る舞いありとする
        return jigInstanceMember.hasMethod();
    }

    public boolean isPolymorphism() {
        // 抽象列挙型は継承クラスがコンパイラに作成されているもので、多態とみなすことにする
        return jigType.getTypeKind() == TypeKind.抽象列挙型;
    }

    public TypeIdentifier typeIdentifier() {
        return jigType.identifier();
    }

    public String nodeLabel() {
        return jigType.getTypeAlias().nodeLabel();
    }

    public String nodeLabel(String delimiter) {
        return jigType.getTypeAlias().nodeLabel(delimiter);
    }
}
