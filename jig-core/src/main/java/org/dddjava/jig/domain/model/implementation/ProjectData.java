package org.dddjava.jig.domain.model.implementation;

import org.dddjava.jig.domain.model.characteristic.CharacterizedMethods;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypeFactory;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotatedFields;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotatedMethods;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.Methods;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodes;
import org.dddjava.jig.domain.model.implementation.bytecode.ImplementationMethods;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodRelations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.networks.type.TypeDependencies;
import org.dddjava.jig.domain.model.values.ValueTypes;

/**
 * プロジェクトから読み取った情報
 */
public class ProjectData {

    // メソッド
    private Methods methods;
    // フィールド
    private StaticFieldDeclarations staticFieldDeclarations;
    private FieldDeclarations fieldDeclarations;
    // アノテーション
    private AnnotatedFields annotatedFields;
    private AnnotatedMethods annotatedMethods;

    // データソースアクセス
    private ImplementationMethods implementationMethods;
    private Sqls sqls;

    // 関連
    private MethodRelations methodRelations;
    private MethodUsingFields methodUsingFields;
    private TypeDependencies typeDependencies;

    // 特徴とセットになったもの
    private ValueTypes valueTypes;
    private CharacterizedTypes characterizedTypes;
    private CharacterizedMethods characterizedMethods;

    public ProjectData(ByteCodes byteCodes, Sqls sqls, CharacterizedTypeFactory characterizedTypeFactory) {
        this.methods = byteCodes.instanceMethods();

        this.annotatedFields = byteCodes.annotatedFields();
        this.annotatedMethods = byteCodes.annotatedMethods();

        this.fieldDeclarations = byteCodes.instanceFields();
        this.staticFieldDeclarations = byteCodes.staticFields();

        this.implementationMethods = new ImplementationMethods(byteCodes);

        this.methodRelations = new MethodRelations(byteCodes);
        this.methodUsingFields = new MethodUsingFields(byteCodes);
        this.typeDependencies = new TypeDependencies(byteCodes);

        CharacterizedTypes characterizedTypes = new CharacterizedTypes(byteCodes, characterizedTypeFactory);
        this.characterizedTypes = characterizedTypes;
        this.characterizedMethods = new CharacterizedMethods(byteCodes.instanceMethodByteCodes(), characterizedTypes);
        this.valueTypes = new ValueTypes(byteCodes);

        this.sqls = sqls;
    }

    public ImplementationMethods implementationMethods() {
        return implementationMethods;
    }

    public MethodRelations methodRelations() {
        return methodRelations;
    }

    public Sqls sqls() {
        return sqls;
    }

    public CharacterizedTypes characterizedTypes() {
        return characterizedTypes;
    }

    public TypeDependencies typeDependencies() {
        return typeDependencies;
    }

    public FieldDeclarations fieldDeclarations() {
        return fieldDeclarations;
    }

    public StaticFieldDeclarations staticFieldDeclarations() {
        return staticFieldDeclarations;
    }

    public ValueTypes valueTypes() {
        return valueTypes;
    }

    public MethodUsingFields methodUsingFields() {
        return methodUsingFields;
    }

    public CharacterizedMethods characterizedMethods() {
        return characterizedMethods;
    }

    public AnnotatedFields annotatedFields() {
        return annotatedFields;
    }

    public AnnotatedMethods annotatedMethods() {
        return annotatedMethods;
    }

    public Methods methods() {
        return methods;
    }
}
