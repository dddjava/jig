package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.declaration.annotation.AnnotationDeclarationRepository;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementation;
import org.dddjava.jig.domain.model.implementation.bytecode.ImplementationFactory;
import org.dddjava.jig.domain.model.implementation.bytecode.ImplementationSources;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementations;
import org.dddjava.jig.domain.model.implementation.relation.RelationRepository;
import org.dddjava.jig.domain.model.networks.DependencyRepository;
import org.springframework.stereotype.Service;

/**
 * 仕様サービス
 */
@Service
public class SpecificationService {

    final ImplementationFactory implementationFactory;
    final RelationRepository relationRepository;
    final AnnotationDeclarationRepository annotationDeclarationRepository;
    final DependencyRepository dependencyRepository;
    final CharacteristicService characteristicService;

    public SpecificationService(ImplementationFactory implementationFactory, CharacteristicService characteristicService, RelationRepository relationRepository, AnnotationDeclarationRepository annotationDeclarationRepository, DependencyRepository dependencyRepository) {
        this.implementationFactory = implementationFactory;
        this.characteristicService = characteristicService;
        this.relationRepository = relationRepository;
        this.annotationDeclarationRepository = annotationDeclarationRepository;
        this.dependencyRepository = dependencyRepository;
    }

    public void importSpecification(ImplementationSources implementationSources, ProjectData projectData) {
        Implementations implementations = specification(implementationSources);

        characteristicService.registerCharacteristic(implementations);

        registerSpecifications(implementations);

        projectData.setCharacterizedTypes(characteristicService.allCharacterizedTypes());
        projectData.setTypeDependencies(dependencyRepository.findAllTypeDependency());
        projectData.setFieldDeclarations(relationRepository.allFieldDeclarations());
        projectData.setStaticFieldDeclarations(relationRepository.allStaticFieldDeclarations());
        projectData.setMapperMethods(characteristicService.getMapperMethods());
        projectData.setImplementationMethods(relationRepository.allImplementationMethods());
        projectData.setMethodRelations(relationRepository.allMethodRelations());
        projectData.setMethodUsingFields(relationRepository.allMethodUsingFields());
        projectData.setValueTypes(characteristicService.valueTypes());
    }

    Implementations specification(ImplementationSources implementationSources) {
        if (implementationSources.notFound()) {
            throw new RuntimeException("解析対象のクラスが存在しないため処理を中断します。");
        }

        return implementationFactory.readFrom(implementationSources);
    }

    void registerSpecifications(Implementations implementations) {
        implementations.list().forEach(this::registerSpecification);

        implementations.instanceMethodSpecifications().forEach(methodSpecification ->
                methodSpecification.methodAnnotationDeclarations().forEach(annotationDeclarationRepository::register));
    }

    void registerSpecification(Implementation implementation) {
        implementation.fieldDeclarations().list().forEach(relationRepository::registerField);
        implementation.staticFieldDeclarations().list().forEach(relationRepository::registerConstants);
        implementation.fieldAnnotationDeclarations().forEach(annotationDeclarationRepository::register);

        implementation.instanceMethodSpecifications().forEach(methodSpecification -> {
            MethodDeclaration methodDeclaration = methodSpecification.methodDeclaration;
            relationRepository.registerMethod(methodDeclaration);

            for (TypeIdentifier interfaceTypeIdentifier : implementation.interfaceTypeIdentifiers.list()) {
                relationRepository.registerImplementation(methodDeclaration, methodDeclaration.with(interfaceTypeIdentifier));
            }

            relationRepository.registerMethodUseFields(methodDeclaration, methodSpecification.usingFields());

            relationRepository.registerMethodUseMethods(methodDeclaration, methodSpecification.usingMethods());
        });

        dependencyRepository.registerDependency(implementation.typeIdentifier(), implementation.useTypes());
    }
}
