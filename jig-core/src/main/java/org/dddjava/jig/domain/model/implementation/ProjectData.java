package org.dddjava.jig.domain.model.implementation;

import org.dddjava.jig.domain.model.categories.Categories;
import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.characteristic.CharacterizedMethods;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypeFactory;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.annotation.FieldAnnotations;
import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.Methods;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.declaration.type.Types;
import org.dddjava.jig.domain.model.implementation.bytecode.*;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.networks.type.TypeDependencies;
import org.dddjava.jig.domain.model.networks.type.TypeDependency;
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

    public ImplementationMethods implementationMethods() {
        List<ImplementationMethod> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            for (MethodByteCode methodByteCode : typeByteCode.instanceMethodByteCodes()) {
                MethodDeclaration methodDeclaration = methodByteCode.methodDeclaration;
                for (TypeIdentifier interfaceTypeIdentifier : typeByteCode.interfaceTypeIdentifiers.list()) {
                    MethodDeclaration implMethod = methodDeclaration.with(interfaceTypeIdentifier);
                    list.add(new ImplementationMethod(methodDeclaration, implMethod));
                }
            }
        }
        return new ImplementationMethods(list);
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

    public TypeDependencies typeDependencies() {
        ArrayList<TypeDependency> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            TypeIdentifier form = typeByteCode.typeIdentifier();
            for (TypeIdentifier to : typeByteCode.useTypes().list()) {
                list.add(new TypeDependency(form, to));
            }
        }
        return new TypeDependencies(list);
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

    public Categories categories() {
        TypeIdentifiers enumTypeIdentifies = characterizedTypes().stream()
                .filter(Characteristic.ENUM)
                .typeIdentifiers();
        return new Categories(enumTypeIdentifies);
    }

    public TypeIdentifiers repositories() {
        return characterizedTypes().stream()
                .filter(Characteristic.REPOSITORY)
                .typeIdentifiers();
    }
}
