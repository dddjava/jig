package org.dddjava.jig.domain.model.categories;

import org.dddjava.jig.domain.basic.ReportItem;
import org.dddjava.jig.domain.basic.ReportItemFor;
import org.dddjava.jig.domain.basic.UserNumber;
import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.characteristic.Characteristics;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.networks.type.TypeDependencies;

/**
 * 区分の切り口
 */
public class CategoryAngle {

    Characteristics characteristics;
    TypeIdentifier typeIdentifier;
    TypeIdentifiers userTypeIdentifiers;
    StaticFieldDeclarations constantsDeclarations;
    FieldDeclarations fieldDeclarations;

    public CategoryAngle(Characteristics characteristics, TypeIdentifier typeIdentifier, TypeIdentifiers userTypeIdentifiers, StaticFieldDeclarations constantsDeclarations, FieldDeclarations fieldDeclarations) {
        this.characteristics = characteristics;
        this.typeIdentifier = typeIdentifier;
        this.userTypeIdentifiers = userTypeIdentifiers;
        this.constantsDeclarations = constantsDeclarations;
        this.fieldDeclarations = fieldDeclarations;
    }

    public static CategoryAngle of(TypeIdentifier typeIdentifier, CharacterizedTypes characterizedTypes, TypeDependencies allTypeDependencies, FieldDeclarations allFieldDeclarations, StaticFieldDeclarations allStaticFieldDeclarations) {
        Characteristics characteristics = characterizedTypes.stream()
                .pickup(typeIdentifier)
                .characteristics();
        TypeIdentifiers userTypeIdentifiers = allTypeDependencies.stream()
                .filterTo(typeIdentifier)
                .removeSelf()
                .fromTypeIdentifiers();
        StaticFieldDeclarations constantsDeclarations = allStaticFieldDeclarations
                .filterDeclareTypeIs(typeIdentifier);
        FieldDeclarations fieldDeclarations = allFieldDeclarations
                .filterDeclareTypeIs(typeIdentifier);
        return new CategoryAngle(characteristics, typeIdentifier, userTypeIdentifiers, constantsDeclarations, fieldDeclarations);
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス和名)
    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    @ReportItemFor(value = ReportItem.汎用文字列, order = 1, label = "定数宣言")
    public String constantsDeclarationsName() {
        return constantsDeclarations.toNameText();
    }

    public StaticFieldDeclarations constantsDeclarations() {
        return constantsDeclarations;
    }

    @ReportItemFor(value = ReportItem.汎用文字列, order = 2, label = "フィールド")
    public String fieldDeclarations() {
        return fieldDeclarations.toSignatureText();
    }

    @ReportItemFor(value = ReportItem.使用箇所数, order = 3)
    public UserNumber userNumber() {
        return new UserNumber(userTypeIdentifiers.list().size());
    }

    @ReportItemFor(value = ReportItem.使用箇所, order = 4)
    public TypeIdentifiers userTypeIdentifiers() {
        return userTypeIdentifiers;
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, order = 5, label = "パラメーター有り")
    public boolean hasParameter() {
        return characteristics.has(Characteristic.ENUM_PARAMETERIZED);
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, order = 6, label = "振る舞い有り")
    public boolean hasBehaviour() {
        return characteristics.has(Characteristic.ENUM_BEHAVIOUR);
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, order = 7, label = "多態")
    public boolean isPolymorphism() {
        return characteristics.has(Characteristic.ENUM_POLYMORPHISM);
    }
}
