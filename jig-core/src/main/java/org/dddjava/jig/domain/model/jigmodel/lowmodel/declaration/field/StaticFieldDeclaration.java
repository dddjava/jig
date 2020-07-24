package org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

/**
 * フィールド定義
 */
public class StaticFieldDeclaration {

    private final TypeIdentifier declaringType;
    String name;
    TypeIdentifier typeIdentifier;

    public StaticFieldDeclaration(TypeIdentifier declaringType, String name, TypeIdentifier typeIdentifier) {
        this.declaringType = declaringType;
        this.name = name;
        this.typeIdentifier = typeIdentifier;
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public String nameText() {
        return name;
    }

    public boolean isSelfDefine() {
        // 定数の型と定義クラスの型が一致するもの
        // enumの列挙、TypeSafe enumパターンを想定
        return typeIdentifier.equals(declaringType);
    }
}
