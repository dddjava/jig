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

import java.util.stream.Stream;

/**
 * サービスの切り口
 */
public class ServiceAngle {

    MethodDeclaration methodDeclaration;
    Characteristics userCharacteristics;

    private final MethodDeclarations userMethods;
    private final MethodDeclarations userServiceMethods;
    private final MethodDeclarations userControllerMethods;

    TypeIdentifiers usingFieldTypeIdentifiers;
    MethodDeclarations usingRepositoryMethods;
    private final MethodCharacteristics methodCharacteristics;
    boolean useStream;

    ServiceAngle(MethodDeclaration serviceMethod, MethodRelations methodRelations, CharacterizedTypes characterizedTypes, MethodUsingFields methodUsingFields, CharacterizedMethods characterizedMethods) {
        this.methodDeclaration = serviceMethod;
        this.userMethods = methodRelations.stream().filterTo(serviceMethod).fromMethods();
        this.userCharacteristics = characterizedTypes.stream()
                .filter(methodRelations.stream().filterTo(serviceMethod).fromTypeIdentifiers())
                .characteristics();
        this.userServiceMethods = methodRelations.stream().filterTo(serviceMethod)
                .filterFromTypeIsIncluded(characterizedTypes.stream().filter(Characteristic.SERVICE).typeIdentifiers())
                .fromMethods();
        this.usingFieldTypeIdentifiers = methodUsingFields.stream()
                .filter(serviceMethod)
                .fields()
                .toTypeIdentifies();
        this.usingRepositoryMethods = methodRelations.stream().filterFrom(serviceMethod)
                .filterToTypeIsIncluded(characterizedTypes.stream().filter(Characteristic.REPOSITORY).typeIdentifiers())
                .toMethods();
        this.methodCharacteristics = characterizedMethods.characteristicsOf(serviceMethod);

        this.userControllerMethods = methodRelations.stream().filterTo(serviceMethod)
                .filterFromTypeIsIncluded(characterizedTypes.stream().filter(Characteristic.CONTROLLER).typeIdentifiers())
                .fromMethods();

        MethodDeclarations usingMethods = methodRelations.stream().filterFrom(serviceMethod).toMethods();
        this.useStream = usingMethods.list().stream().anyMatch(methodDeclaration -> methodDeclaration.returnType().equals(new TypeIdentifier(Stream.class)));
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

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "stream使用", order = 3)
    public boolean useStream() {
        return useStream;
    }

    public MethodDeclarations userServiceMethods() {
        return userServiceMethods;
    }

    public MethodCharacteristics methodCharacteristics() {
        return methodCharacteristics;
    }

    public MethodDeclarations userMethods() {
        return userMethods;
    }

    public MethodDeclarations userControllerMethods() {
        return userControllerMethods;
    }

}
