package org.dddjava.jig.domain.model.services;

import org.dddjava.jig.domain.model.characteristic.*;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodRelations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;
import org.dddjava.jig.domain.model.report.ReportItem;
import org.dddjava.jig.domain.model.report.ReportItemFor;

/**
 * サービスの切り口
 */
public class ServiceAngle {

    MethodDeclaration methodDeclaration;
    Characteristics userCharacteristics;
    private final MethodDeclarations userServiceMethods;
    TypeIdentifiers usingFieldTypeIdentifiers;
    MethodDeclarations usingRepositoryMethods;
    private final MethodCharacteristics methodCharacteristics;

    public ServiceAngle(MethodDeclaration methodDeclaration, Characteristics userCharacteristics, MethodDeclarations userServiceMethods, TypeIdentifiers usingFieldTypeIdentifiers, MethodDeclarations usingRepositoryMethods, MethodCharacteristics methodCharacteristics) {
        this.methodDeclaration = methodDeclaration;
        this.userCharacteristics = userCharacteristics;
        this.userServiceMethods = userServiceMethods;
        this.usingFieldTypeIdentifiers = usingFieldTypeIdentifiers;
        this.usingRepositoryMethods = usingRepositoryMethods;
        this.methodCharacteristics = methodCharacteristics;
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス和名)
    public TypeIdentifier declaringType() {
        return methodDeclaration.declaringType();
    }

    @ReportItemFor(ReportItem.メソッド名)
    @ReportItemFor(ReportItem.メソッド和名)
    @ReportItemFor(ReportItem.メソッド戻り値の型)
    @ReportItemFor(ReportItem.メソッド戻り値の型の和名)
    @ReportItemFor(ReportItem.メソッド引数の型の和名)
    public MethodDeclaration method() {
        return methodDeclaration;
    }

    @ReportItemFor(ReportItem.イベントハンドラ)
    public boolean usingFromController() {
        // TODO MethodCharacteristic.HANDLERで判別させたい
        return userCharacteristics.has(Characteristic.CONTROLLER);
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "使用しているフィールドの型", order = 1)
    public String usingFields() {
        return usingFieldTypeIdentifiers.asSimpleText();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "使用しているリポジトリのメソッド", order = 2)
    public String usingRepositoryMethods() {
        return usingRepositoryMethods.asSimpleText();
    }

    public MethodDeclarations userServiceMethods() {
        return userServiceMethods;
    }

    public static ServiceAngle of(MethodDeclaration serviceMethod, MethodRelations methodRelations, CharacterizedTypes characterizedTypes, MethodUsingFields methodUsingFields, CharacterizedMethods characterizedMethods) {

        Characteristics userCharacteristics = characterizedTypes.stream()
                .filter(methodRelations.stream().filterTo(serviceMethod).fromTypeIdentifiers())
                .characteristics();

        MethodDeclarations userServiceMethods = methodRelations.stream().filterTo(serviceMethod)
                .filterFromTypeIsIncluded(characterizedTypes.stream().filter(Characteristic.SERVICE).typeIdentifiers())
                .fromMethods();

        TypeIdentifiers usingFieldTypeIdentifiers = methodUsingFields.stream()
                .filter(serviceMethod)
                .fields()
                .toTypeIdentifies();

        MethodDeclarations usingRepositoryMethods = methodRelations.stream().filterFrom(serviceMethod)
                .filterToTypeIsIncluded(characterizedTypes.stream().filter(Characteristic.REPOSITORY).typeIdentifiers())
                .toMethods();

        MethodCharacteristics methodCharacteristics = characterizedMethods.characteristicsOf(serviceMethod);

        return new ServiceAngle(serviceMethod, userCharacteristics, userServiceMethods, usingFieldTypeIdentifiers, usingRepositoryMethods, methodCharacteristics);
    }

    public MethodCharacteristics methodCharacteristics() {
        return methodCharacteristics;
    }
}
