package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.declaration.annotation.AnnotationDeclarationRepository;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.MethodImplementation;
import org.dddjava.jig.domain.model.implementation.Implementation;
import org.dddjava.jig.domain.model.implementation.Implementations;
import org.dddjava.jig.infrastructure.PropertyImplementationAnalyzeContext;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryRelationRepository;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ImplementationServiceRegisterImplementationsTest {

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
        Implementation implementation = new Implementation(
                new PropertyImplementationAnalyzeContext(),
                typeIdentifier,
                new TypeIdentifier("test.TestParentClass"),
                new TypeIdentifiers(emptyList()), emptyList(), false);
        MethodImplementation methodImplementation = new MethodImplementation(
                new MethodDeclaration(typeIdentifier, new MethodSignature("methodName", emptyList()), new TypeIdentifier("test.ReturnType")),
                new TypeIdentifier("test.ReturnType"),
                emptyList(),
                0);
        // フィールド呼び出し
        methodImplementation.registerFieldInstruction(implementation.newFieldDeclaration("field1", new TypeIdentifier("test.FieldA")));
        methodImplementation.registerFieldInstruction(new FieldDeclaration(new TypeIdentifier("test.OtherClass1"), "field2", new TypeIdentifier("test.FieldB")));
        methodImplementation.registerFieldInstruction(implementation.newFieldDeclaration("field3", new TypeIdentifier("test.FieldA")));
        methodImplementation.registerFieldInstruction(implementation.newFieldDeclaration("field4", new TypeIdentifier("test.FieldB")));
        // メソッド呼び出し
        methodImplementation.registerMethodInstruction(new MethodDeclaration(typeIdentifier, new MethodSignature("methodA", emptyList()), new TypeIdentifier("test.MethodReturn1")));
        methodImplementation.registerMethodInstruction(new MethodDeclaration(new TypeIdentifier("test.OtherClass2"), new MethodSignature("methodB", emptyList()), new TypeIdentifier("test.MethodReturn2")));
        methodImplementation.registerMethodInstruction(new MethodDeclaration(typeIdentifier, new MethodSignature("methodA", emptyList()), new TypeIdentifier("test.MethodReturn1")));

        implementation.registerInstanceMethodSpecification(methodImplementation);

        sut.registerSpecifications(new Implementations(Collections.singletonList(implementation)));

        MethodDeclaration methodDeclaration = new MethodDeclaration(
                new TypeIdentifier("test.TestClass"),
                new MethodSignature("methodName", emptyList()),
                new TypeIdentifier("test.ReturnType"));
        // メソッドの使用しているフィールドがわかる
        assertThat(relationRepository.findUseFields(methodDeclaration).toTypeIdentifies().asText())
                .isEqualTo("[test.FieldA, test.FieldB]");
        // メソッドの使用しているメソッドがわかる
        assertThat(relationRepository.findUseMethods(methodDeclaration).asSimpleText())
                .isEqualTo("[OtherClass2.methodB(), TestClass.methodA()]");
    }
}