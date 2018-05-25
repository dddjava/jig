package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.characteristic.CharacterizedMethods;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotationDeclarationRepository;
import org.dddjava.jig.domain.model.declaration.annotation.FieldAnnotationDeclaration;
import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotationDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.implementation.bytecode.*;
import org.dddjava.jig.domain.model.implementation.relation.RelationRepository;
import org.dddjava.jig.domain.model.networks.TypeDependencies;
import org.dddjava.jig.domain.model.values.ValueTypes;
import org.springframework.stereotype.Service;

/**
 * 仕様サービス
 */
@Service
public class SpecificationService {

    final ImplementationFactory implementationFactory;
    final RelationRepository relationRepository;
    final AnnotationDeclarationRepository annotationDeclarationRepository;

    public SpecificationService(ImplementationFactory implementationFactory, RelationRepository relationRepository, AnnotationDeclarationRepository annotationDeclarationRepository) {
        this.implementationFactory = implementationFactory;
        this.relationRepository = relationRepository;
        this.annotationDeclarationRepository = annotationDeclarationRepository;
    }

    public ProjectData importSpecification(ImplementationSources implementationSources, ProjectData projectData) {
        if (implementationSources.notFound()) {
            throw new RuntimeException("解析対象のクラスが存在しないため処理を中断します。");
        }

        Implementations implementations = implementationFactory.readFrom(implementationSources);

        for (Implementation implementation : implementations.list()) {
            for (FieldDeclaration fieldDeclaration : implementation.fieldDeclarations().list()) {
                relationRepository.registerField(fieldDeclaration);
            }
            for (FieldDeclaration fieldDeclaration : implementation.staticFieldDeclarations().list()) {
                relationRepository.registerConstants(fieldDeclaration);
            }
            for (FieldAnnotationDeclaration fieldAnnotationDeclaration : implementation.fieldAnnotationDeclarations()) {
                annotationDeclarationRepository.register(fieldAnnotationDeclaration);
            }

            for (MethodImplementation methodSpecification : implementation.instanceMethodSpecifications()) {
                MethodDeclaration methodDeclaration = methodSpecification.methodDeclaration;

                for (TypeIdentifier interfaceTypeIdentifier : implementation.interfaceTypeIdentifiers.list()) {
                    relationRepository.registerImplementation(methodDeclaration, methodDeclaration.with(interfaceTypeIdentifier));
                }

                relationRepository.registerMethodUseFields(methodDeclaration, methodSpecification.usingFields());

                relationRepository.registerMethodUseMethods(methodDeclaration, methodSpecification.usingMethods());
            }
        }

        for (MethodImplementation methodSpecification : implementations.instanceMethodSpecifications()) {
            for (MethodAnnotationDeclaration methodAnnotationDeclaration : methodSpecification.methodAnnotationDeclarations()) {
                annotationDeclarationRepository.register(methodAnnotationDeclaration);
            }
        }

        projectData.setFieldDeclarations(relationRepository.allFieldDeclarations());
        projectData.setStaticFieldDeclarations(relationRepository.allStaticFieldDeclarations());
        projectData.setImplementationMethods(relationRepository.allImplementationMethods());
        projectData.setMethodRelations(relationRepository.allMethodRelations());
        projectData.setMethodUsingFields(relationRepository.allMethodUsingFields());

        projectData.setTypeDependencies(new TypeDependencies(implementations));
        projectData.setCharacterizedTypes(new CharacterizedTypes(implementations));
        projectData.setCharacterizedMethods(new CharacterizedMethods(implementations.instanceMethodSpecifications()));
        projectData.setValueTypes(new ValueTypes(implementations));

        return projectData;
    }
}
