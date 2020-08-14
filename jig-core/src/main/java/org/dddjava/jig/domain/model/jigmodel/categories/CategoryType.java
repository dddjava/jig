package org.dddjava.jig.domain.model.jigmodel.categories;

import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.TypeKind;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

/**
 * 区分
 */
public class CategoryType {
    BusinessRule businessRule;

    public CategoryType(BusinessRule businessRule) {
        this.businessRule = businessRule;
    }

    public boolean hasParameter() {
        return businessRule.jigInstanceMember().hasField();
    }

    public boolean hasBehaviour() {
        // インスタンスメソッドがあるものを振る舞いありとする
        return businessRule.jigInstanceMember().hasMethod();
    }

    public boolean isPolymorphism() {
        // 抽象列挙型は継承クラスがコンパイラに作成されているもので、多態とみなすことにする
        return businessRule.jigType().typeKind() == TypeKind.抽象列挙型;
    }

    public TypeIdentifier typeIdentifier() {
        return businessRule.typeIdentifier();
    }

    public String nodeLabel() {
        return businessRule.jigType().typeAlias().nodeLabel();
    }

    public String nodeLabel(String delimiter) {
        return businessRule.jigType().typeAlias().nodeLabel(delimiter);
    }

    public StaticFieldDeclarations constantsDeclarations() {
        return businessRule.jigTypeMember().staticFieldDeclarations();
    }

    public FieldDeclarations fieldDeclarations() {
        return businessRule.jigInstanceMember().fieldDeclarations();
    }

    public boolean markedCore() {
        return businessRule.jigType().typeAlias().markedCore();
    }
}
