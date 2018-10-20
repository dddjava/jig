package org.dddjava.jig.domain.model.implementation;

import org.dddjava.jig.domain.model.categories.CategoryCharacteristic;
import org.dddjava.jig.domain.model.categories.CategoryCharacteristics;
import org.dddjava.jig.domain.model.categories.CategoryType;
import org.dddjava.jig.domain.model.categories.CategoryTypes;
import org.dddjava.jig.domain.model.characteristic.*;
import org.dddjava.jig.domain.model.declaration.annotation.FieldAnnotations;
import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.declaration.type.Types;
import org.dddjava.jig.domain.model.implementation.bytecode.*;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.unit.method.Methods;
import org.dddjava.jig.domain.model.values.PotentiallyValueType;
import org.dddjava.jig.domain.model.values.PotentiallyValueTypes;
import org.dddjava.jig.domain.model.values.ValueTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * プロジェクトから読み取った情報
 */
public class ProjectData {

    private final TypeByteCodes typeByteCodes;

    private Types types;
    // メソッド
    private Methods methods;
    // フィールド
    private StaticFieldDeclarations staticFieldDeclarations;
    private FieldDeclarations fieldDeclarations;
    // アノテーション
    private TypeAnnotations typeAnnotations;
    private FieldAnnotations fieldAnnotations;
    private MethodAnnotations methodAnnotations;

    // データソースアクセス
    private Sqls sqls;

    // 特徴とセットになったもの
    private CharacterizedTypes characterizedTypes;
    private CharacterizedMethods characterizedMethods;

    public ProjectData(TypeByteCodes typeByteCodes, Sqls sqls, CharacterizedTypeFactory characterizedTypeFactory) {
        this.typeByteCodes = typeByteCodes;
        this.types = typeByteCodes.types();
        this.methods = typeByteCodes.instanceMethods();

        this.typeAnnotations = typeByteCodes.typeAnnotations();
        this.fieldAnnotations = typeByteCodes.annotatedFields();
        this.methodAnnotations = typeByteCodes.annotatedMethods();

        this.fieldDeclarations = typeByteCodes.instanceFields();
        this.staticFieldDeclarations = typeByteCodes.staticFields();

        CharacterizedTypes characterizedTypes = new CharacterizedTypes(typeByteCodes, characterizedTypeFactory);
        this.characterizedTypes = characterizedTypes;
        this.characterizedMethods = new CharacterizedMethods(typeByteCodes.instanceMethodByteCodes(), characterizedTypes);

        this.sqls = sqls;
    }

    public MethodRelations methodRelations() {
        List<MethodRelation> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            for (MethodByteCode methodByteCode : typeByteCode.methodByteCodes()) {
                MethodDeclaration methodDeclaration = methodByteCode.methodDeclaration;
                for (MethodDeclaration usingMethod : methodByteCode.usingMethods().list()) {
                    list.add(new MethodRelation(methodDeclaration, usingMethod));
                }
            }
        }
        return new MethodRelations(list);
    }

    public Sqls sqls() {
        return sqls;
    }

    public CharacterizedTypes characterizedTypes() {
        return characterizedTypes;
    }

    public FieldDeclarations fieldDeclarations() {
        return fieldDeclarations;
    }

    public StaticFieldDeclarations staticFieldDeclarations() {
        return staticFieldDeclarations;
    }

    public ValueTypes valueTypes() {
        ArrayList<PotentiallyValueType> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            list.add(new PotentiallyValueType(typeByteCode.typeIdentifier(), typeByteCode.fieldDeclarations()));
        }
        PotentiallyValueTypes potentiallyValueTypes = new PotentiallyValueTypes(list);

        return potentiallyValueTypes.toValueTypes(categories());
    }

    public MethodUsingFields methodUsingFields() {
        ArrayList<MethodUsingField> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            for (MethodByteCode methodByteCode : typeByteCode.instanceMethodByteCodes()) {
                MethodDeclaration methodDeclaration = methodByteCode.methodDeclaration;
                for (FieldDeclaration usingField : methodByteCode.usingFields().list()) {
                    list.add(new MethodUsingField(methodDeclaration, usingField));
                }
            }
        }
        return new MethodUsingFields(list);
    }

    public CharacterizedMethods characterizedMethods() {
        return characterizedMethods;
    }

    public TypeAnnotations typeAnnotations() {
        return typeAnnotations;
    }

    public FieldAnnotations fieldAnnotations() {
        return fieldAnnotations;
    }

    public MethodAnnotations methodAnnotations() {
        return methodAnnotations;
    }

    public Types types() {
        return types;
    }

    public Methods methods() {
        return methods;
    }

    public Methods controllerMethods() {
        return methods().controllerMethods(characterizedTypes);
    }

    public CategoryTypes categories() {
        TypeIdentifiers enumTypeIdentifies = characterizedTypes().stream()
                .filter(Characteristic.ENUM)
                .typeIdentifiers();

        ArrayList<CategoryType> list = new ArrayList<>();
        for (TypeIdentifier typeIdentifier : enumTypeIdentifies.list()) {
            Characteristics characteristics = characterizedTypes.stream()
                    .pickup(typeIdentifier)
                    .characteristics();

            // TODO ifなくしたいけどCharacteristicをなくしてから考える
            List<CategoryCharacteristic> categoryCharacteristicList = new ArrayList<>();
            if (characteristics.has(Characteristic.ENUM_BEHAVIOUR)) {
                categoryCharacteristicList.add(CategoryCharacteristic.BEHAVIOUR);
            }
            if (characteristics.has(Characteristic.ENUM_PARAMETERIZED)) {
                categoryCharacteristicList.add(CategoryCharacteristic.PARAMETERIZED);
            }
            if (characteristics.has(Characteristic.ENUM_POLYMORPHISM)) {
                categoryCharacteristicList.add(CategoryCharacteristic.POLYMORPHISM);
            }

            list.add(new CategoryType(typeIdentifier, new CategoryCharacteristics(categoryCharacteristicList)));
        }

        return new CategoryTypes(list);
    }

    public TypeIdentifiers repositories() {
        return characterizedTypes().stream()
                .filter(Characteristic.REPOSITORY)
                .typeIdentifiers();
    }

    public TypeByteCodes typeByteCodes() {
        return typeByteCodes;
    }
}
