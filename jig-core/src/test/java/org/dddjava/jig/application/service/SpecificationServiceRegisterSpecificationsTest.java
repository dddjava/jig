package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.declaration.annotation.AnnotationDeclarationRepository;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.specification.MethodSpecification;
import org.dddjava.jig.domain.model.specification.Specification;
import org.dddjava.jig.domain.model.specification.Specifications;
import org.dddjava.jig.infrastructure.PropertySpecificationContext;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryRelationRepository;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SpecificationServiceRegisterSpecificationsTest {

    @Test
    void メソッドの使用するメソッドを取得する() {
        OnMemoryRelationRepository relationRepository = new OnMemoryRelationRepository();

        SpecificationService sut = new SpecificationService(
                null,
                mock(CharacteristicService.class),
                relationRepository,
                mock(AnnotationDeclarationRepository.class),
                mock(DependencyService.class));

        TypeIdentifier typeIdentifier = new TypeIdentifier("test.TestClass");
        Specification specification = new Specification(
                new PropertySpecificationContext(),
                typeIdentifier,
                new TypeIdentifier("test.TestParentClass"),
                new TypeIdentifiers(emptyList()), emptyList(), false);
        MethodSpecification methodSpecification = new MethodSpecification(
                new MethodDeclaration(typeIdentifier, new MethodSignature("methodName", emptyList()), new TypeIdentifier("test.ReturnType")),
                new TypeIdentifier("test.ReturnType"),
                emptyList());
        // フィールド呼び出し
        methodSpecification.registerFieldInstruction(specification.newFieldDeclaration("field1", new TypeIdentifier("test.FieldA")));
        methodSpecification.registerFieldInstruction(new FieldDeclaration(new TypeIdentifier("test.OtherClass1"), "field2", new TypeIdentifier("test.FieldB")));
        methodSpecification.registerFieldInstruction(specification.newFieldDeclaration("field3", new TypeIdentifier("test.FieldA")));
        methodSpecification.registerFieldInstruction(specification.newFieldDeclaration("field4", new TypeIdentifier("test.FieldB")));
        // メソッド呼び出し
        methodSpecification.registerMethodInstruction(new MethodDeclaration(typeIdentifier, new MethodSignature("methodA", emptyList()), new TypeIdentifier("test.MethodReturn1")));
        methodSpecification.registerMethodInstruction(new MethodDeclaration(new TypeIdentifier("test.OtherClass2"), new MethodSignature("methodB", emptyList()), new TypeIdentifier("test.MethodReturn2")));
        methodSpecification.registerMethodInstruction(new MethodDeclaration(typeIdentifier, new MethodSignature("methodA", emptyList()), new TypeIdentifier("test.MethodReturn1")));

        specification.registerInstanceMethodSpecification(methodSpecification);

        sut.registerSpecifications(new Specifications(Collections.singletonList(specification)));

        MethodDeclaration methodDeclaration = new MethodDeclaration(
                new TypeIdentifier("test.TestClass"),
                new MethodSignature("methodName", emptyList()),
                new TypeIdentifier("test.ReturnType"));
        // メソッドの使用しているフィールドがわかる
        assertThat(relationRepository.findUseFields(methodDeclaration).toTypeIdentifies().asText())
                .isEqualTo("[test.FieldA, test.FieldB]");
        // メソッドの使用しているメソッドがわかる
        assertThat(relationRepository.findUseMethod(methodDeclaration).asSimpleText())
                .isEqualTo("[OtherClass2.methodB(), TestClass.methodA()]");
    }
}