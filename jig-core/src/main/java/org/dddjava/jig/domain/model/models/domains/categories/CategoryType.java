package org.dddjava.jig.domain.model.models.domains.categories;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.class_.TypeKind;
import org.dddjava.jig.domain.model.parts.classes.field.FieldDeclarations;
import org.dddjava.jig.domain.model.parts.classes.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

/**
 * 区分
 */
public class CategoryType {

    JigType jigType;

    public CategoryType(JigType jigType) {
        this.jigType = jigType;
    }

    public JigType jigType() {
        return jigType;
    }

    public boolean hasParameter() {
        return jigType.instanceMember().hasField();
    }

    public boolean hasBehaviour() {
        // インスタンスメソッドがあるものを振る舞いありとする
        return jigType.instanceMember().hasMethod();
    }

    public boolean isPolymorphism() {
        // 抽象列挙型は継承クラスがコンパイラに作成されているもので、多態とみなすことにする
        return jigType.typeKind() == TypeKind.抽象列挙型;
    }

    public TypeIdentifier typeIdentifier() {
        return jigType.identifier();
    }

    public String nodeLabel() {
        return jigType.typeAlias().nodeLabel();
    }

    public String nodeLabel(String delimiter) {
        return jigType.typeAlias().nodeLabel(delimiter);
    }

    public StaticFieldDeclarations constantsDeclarations() {
        return jigType.staticMember().staticFieldDeclarations();
    }

    public FieldDeclarations fieldDeclarations() {
        return jigType.instanceMember().fieldDeclarations();
    }

    public StaticFieldDeclarations values() {
        return constantsDeclarations().selfDefineOnly();
    }
}
