package jig.application.service;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.declaration.annotation.AnnotationDeclarationRepository;
import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.identifier.namespace.PackageIdentifiers;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.dependency.PackageDependencies;
import jig.domain.model.relation.dependency.PackageDependency;
import jig.domain.model.specification.Specification;
import jig.domain.model.specification.Specifications;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DependencyService {

    private final CharacteristicRepository characteristicRepository;
    private final RelationRepository relationRepository;
    private final AnnotationDeclarationRepository annotationDeclarationRepository;

    public DependencyService(CharacteristicRepository characteristicRepository, RelationRepository relationRepository, AnnotationDeclarationRepository annotationDeclarationRepository) {
        this.characteristicRepository = characteristicRepository;
        this.relationRepository = relationRepository;
        this.annotationDeclarationRepository = annotationDeclarationRepository;
    }

    public PackageDependencies packageDependencies() {
        TypeIdentifiers modelTypes = characteristicRepository.getTypeIdentifiersOf(Characteristic.MODEL);
        List<PackageDependency> list =
                modelTypes.list().stream()
                        .flatMap(identifier -> relationRepository.findDependency(identifier)
                                .filter(usage -> characteristicRepository.has(usage, Characteristic.MODEL))
                                .list().stream()
                                .map(TypeIdentifier::packageIdentifier)
                                .map(usePackage -> new PackageDependency(identifier.packageIdentifier(), usePackage))
                                .filter(PackageDependency::notSelfRelation))
                        .distinct()
                        .collect(Collectors.toList());

        PackageIdentifiers allPackages = new PackageIdentifiers(
                modelTypes.list().stream()
                        .map(TypeIdentifier::packageIdentifier)
                        .collect(Collectors.toList()));

        return new PackageDependencies(list, allPackages);
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

        relationRepository.registerDependency(specification.typeIdentifier(), specification.useTypes());
    }
}
