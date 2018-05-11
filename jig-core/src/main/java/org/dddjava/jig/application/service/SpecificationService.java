package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.declaration.annotation.AnnotationDeclarationRepository;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.relation.RelationRepository;
import org.dddjava.jig.domain.model.implementation.Implementation;
import org.dddjava.jig.domain.model.implementation.ImplementationFactory;
import org.dddjava.jig.domain.model.implementation.ImplementationSources;
import org.dddjava.jig.domain.model.implementation.Implementations;
import org.springframework.stereotype.Service;

/**
 * 仕様サービス
 */
@Service
public class SpecificationService {

    final ImplementationFactory implementationFactory;
    final RelationRepository relationRepository;
    final AnnotationDeclarationRepository annotationDeclarationRepository;
    final DependencyService dependencyService;
    final CharacteristicService characteristicService;

    public SpecificationService(ImplementationFactory implementationFactory, CharacteristicService characteristicService, RelationRepository relationRepository, AnnotationDeclarationRepository annotationDeclarationRepository, DependencyService dependencyService) {
        this.implementationFactory = implementationFactory;
        this.characteristicService = characteristicService;
        this.relationRepository = relationRepository;
        this.annotationDeclarationRepository = annotationDeclarationRepository;
        this.dependencyService = dependencyService;
    }

    public void importSpecification(ImplementationSources implementationSources) {
        Implementations implementations = specification(implementationSources);

        characteristicService.registerCharacteristic(implementations);

        registerSpecifications(implementations);
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

        dependencyService.registerDependency(implementation.typeIdentifier(), implementation.useTypes());
    }
}
