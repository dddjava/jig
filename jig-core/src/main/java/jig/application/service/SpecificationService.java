package jig.application.service;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.declaration.annotation.AnnotationDeclarationRepository;
import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.specification.Specification;
import jig.domain.model.specification.SpecificationReader;
import jig.domain.model.specification.SpecificationSources;
import jig.domain.model.specification.Specifications;
import org.springframework.stereotype.Service;

@Service
public class SpecificationService {

    final SpecificationReader specificationReader;
    final CharacteristicRepository characteristicRepository;
    final RelationRepository relationRepository;
    final AnnotationDeclarationRepository annotationDeclarationRepository;
    private final DependencyService dependencyService;

    public SpecificationService(SpecificationReader specificationReader, CharacteristicRepository characteristicRepository, RelationRepository relationRepository, AnnotationDeclarationRepository annotationDeclarationRepository, DependencyService dependencyService) {
        this.specificationReader = specificationReader;
        this.characteristicRepository = characteristicRepository;
        this.relationRepository = relationRepository;
        this.annotationDeclarationRepository = annotationDeclarationRepository;
        this.dependencyService = dependencyService;
    }

    public void importSpecification(SpecificationSources specificationSources) {
        Specifications specifications = specification(specificationSources);
        registerSpecifications(specifications);
    }

    public Specifications specification(SpecificationSources specificationSources) {
        if (specificationSources.notFound()) {
            throw new RuntimeException("解析対象のクラスが存在しないため処理を中断します。");
        }

        return specificationReader.readFrom(specificationSources);
    }

    public void registerSpecifications(Specifications specifications) {
        specifications.list().forEach(this::registerSpecification);

        specifications.instanceMethodSpecifications().forEach(methodSpecification ->
                methodSpecification.methodAnnotationDeclarations().forEach(annotationDeclarationRepository::register));
    }

    public void registerSpecification(Specification specification) {
        characteristicRepository.register(Characteristic.resolveCharacteristics(specification));
        specification.fieldIdentifiers().list().forEach(relationRepository::registerField);
        specification.staticFieldDeclarations().list().forEach(relationRepository::registerConstants);
        specification.fieldAnnotationDeclarations().forEach(annotationDeclarationRepository::register);

        specification.instanceMethodSpecifications().forEach(methodSpecification -> {
            MethodDeclaration methodDeclaration = methodSpecification.methodDeclaration;
            relationRepository.registerMethod(methodDeclaration);
            relationRepository.registerMethodParameter(methodDeclaration);
            relationRepository.registerMethodReturnType(methodDeclaration, methodSpecification.returnType());

            for (TypeIdentifier interfaceTypeIdentifier : specification.interfaceTypeIdentifiers.list()) {
                relationRepository.registerImplementation(methodDeclaration, methodDeclaration.with(interfaceTypeIdentifier));
            }

            relationRepository.registerMethodUseFields(methodDeclaration, methodSpecification.usingFields());

            relationRepository.registerMethodUseMethods(methodDeclaration, methodSpecification.usingMethods());
        });

        dependencyService.registerDependency(specification.typeIdentifier(), specification.useTypes());
    }
}
