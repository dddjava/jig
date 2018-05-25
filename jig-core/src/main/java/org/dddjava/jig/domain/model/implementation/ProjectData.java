package org.dddjava.jig.domain.model.implementation;

import org.dddjava.jig.domain.model.characteristic.CharacterizedMethods;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotatedFields;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotatedMethods;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodes;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.implementation.relation.ImplementationMethods;
import org.dddjava.jig.domain.model.implementation.relation.MethodRelations;
import org.dddjava.jig.domain.model.networks.TypeDependencies;
import org.dddjava.jig.domain.model.values.ValueTypes;

/**
 * プロジェクトから読み取った情報
 *
 * TODO 名前や構成を見直す
 */
public class ProjectData {

    // フィールド
    private FieldDeclarations staticFieldDeclarations;
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

    /**
     * 実装をプロジェクトデータに変換する
     */
    public static ProjectData from(ByteCodes byteCodes, Sqls sqls) {
        ProjectData projectData = new ProjectData();
        projectData.setAnnotatedFields(new AnnotatedFields(byteCodes));
        projectData.setAnnotatedMethods(new AnnotatedMethods(byteCodes));

        projectData.setFieldDeclarations(FieldDeclarations.ofInstanceField(byteCodes));
        projectData.setStaticFieldDeclarations(FieldDeclarations.ofStaticField(byteCodes));
        projectData.setImplementationMethods(new ImplementationMethods(byteCodes));
        projectData.setMethodRelations(new MethodRelations(byteCodes));
        projectData.setMethodUsingFields(new MethodUsingFields(byteCodes));

        projectData.setTypeDependencies(new TypeDependencies(byteCodes));
        projectData.setCharacterizedTypes(new CharacterizedTypes(byteCodes));
        projectData.setCharacterizedMethods(new CharacterizedMethods(byteCodes.instanceMethodSpecifications()));
        projectData.setValueTypes(new ValueTypes(byteCodes));

        projectData.setSqls(sqls);

        return projectData;
    }


    public void setImplementationMethods(ImplementationMethods implementationMethods) {
        this.implementationMethods = implementationMethods;
    }

    public void setMethodRelations(MethodRelations methodRelations) {
        this.methodRelations = methodRelations;
    }

    public void setSqls(Sqls sqls) {
        this.sqls = sqls;
    }

    public void setMethodUsingFields(MethodUsingFields methodUsingFields) {
        this.methodUsingFields = methodUsingFields;
    }

    public void setValueTypes(ValueTypes valueTypes) {
        this.valueTypes = valueTypes;
    }

    public void setStaticFieldDeclarations(FieldDeclarations staticFieldDeclarations) {
        this.staticFieldDeclarations = staticFieldDeclarations;
    }

    public void setCharacterizedTypes(CharacterizedTypes characterizedTypes) {
        this.characterizedTypes = characterizedTypes;
    }

    public void setTypeDependencies(TypeDependencies typeDependencies) {
        this.typeDependencies = typeDependencies;
    }

    public void setFieldDeclarations(FieldDeclarations fieldDeclarations) {
        this.fieldDeclarations = fieldDeclarations;
    }

    public void setCharacterizedMethods(CharacterizedMethods characterizedMethods) {
        this.characterizedMethods = characterizedMethods;
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

    public FieldDeclarations staticFieldDeclarations() {
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

    public void setAnnotatedFields(AnnotatedFields annotatedFields) {
        this.annotatedFields = annotatedFields;
    }

    public void setAnnotatedMethods(AnnotatedMethods annotatedMethods) {
        this.annotatedMethods = annotatedMethods;
    }

    public AnnotatedFields annotatedFields() {
        return annotatedFields;
    }

    public AnnotatedMethods annotatedMethods() {
        return annotatedMethods;
    }
}
