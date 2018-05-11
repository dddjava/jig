package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.declaration.annotation.AnnotationDeclarationRepository;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.relation.RelationRepository;
import org.dddjava.jig.domain.model.implementation.Specification;
import org.dddjava.jig.domain.model.implementation.SpecificationReader;
import org.dddjava.jig.domain.model.implementation.SpecificationSources;
import org.dddjava.jig.domain.model.implementation.Specifications;
import org.springframework.stereotype.Service;

/**
 * 仕様サービス
 */
@Service
public class SpecificationService {

    final SpecificationReader specificationReader;
    final RelationRepository relationRepository;
    final AnnotationDeclarationRepository annotationDeclarationRepository;
    final DependencyService dependencyService;
    final CharacteristicService characteristicService;

    public SpecificationService(SpecificationReader specificationReader, CharacteristicService characteristicService, RelationRepository relationRepository, AnnotationDeclarationRepository annotationDeclarationRepository, DependencyService dependencyService) {
        this.specificationReader = specificationReader;
        this.characteristicService = characteristicService;
        this.relationRepository = relationRepository;
        this.annotationDeclarationRepository = annotationDeclarationRepository;
        this.dependencyService = dependencyService;
    }

    public void importSpecification(SpecificationSources specificationSources) {
        Specifications specifications = specification(specificationSources);

        characteristicService.registerCharacteristic(specifications);

        registerSpecifications(specifications);
    }

    Specifications specification(SpecificationSources specificationSources) {
        if (specificationSources.notFound()) {
            throw new RuntimeException("解析対象のクラスが存在しないため処理を中断します。");
        }

        return specificationReader.readFrom(specificationSources);
    }

    void registerSpecifications(Specifications specifications) {
        specifications.list().forEach(this::registerSpecification);

        specifications.instanceMethodSpecifications().forEach(methodSpecification ->
                methodSpecification.methodAnnotationDeclarations().forEach(annotationDeclarationRepository::register));
    }

    void registerSpecification(Specification specification) {
        specification.fieldDeclarations().list().forEach(relationRepository::registerField);
        specification.staticFieldDeclarations().list().forEach(relationRepository::registerConstants);
        specification.fieldAnnotationDeclarations().forEach(annotationDeclarationRepository::register);

        specification.instanceMethodSpecifications().forEach(methodSpecification -> {
            MethodDeclaration methodDeclaration = methodSpecification.methodDeclaration;
            relationRepository.registerMethod(methodDeclaration);

            for (TypeIdentifier interfaceTypeIdentifier : specification.interfaceTypeIdentifiers.list()) {
                relationRepository.registerImplementation(methodDeclaration, methodDeclaration.with(interfaceTypeIdentifier));
            }

            relationRepository.registerMethodUseFields(methodDeclaration, methodSpecification.usingFields());

            relationRepository.registerMethodUseMethods(methodDeclaration, methodSpecification.usingMethods());
        });

        dependencyService.registerDependency(specification.typeIdentifier(), specification.useTypes());
    }
}
