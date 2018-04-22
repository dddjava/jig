package jig.application.service;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.identifier.namespace.PackageIdentifiers;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.dependency.DependencyRepository;
import jig.domain.model.relation.dependency.PackageDependencies;
import jig.domain.model.relation.dependency.PackageDependency;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DependencyService {

    private final CharacteristicRepository characteristicRepository;
    private final DependencyRepository dependencyRepository;

    public DependencyService(CharacteristicRepository characteristicRepository, RelationRepository relationRepository) {
        this.characteristicRepository = characteristicRepository;
        this.dependencyRepository = new DependencyRepository();
    }

    public PackageDependencies packageDependencies() {
        TypeIdentifiers modelTypes = characteristicRepository.getTypeIdentifiersOf(Characteristic.MODEL);
        List<PackageDependency> list =
                modelTypes.list().stream()
                        .flatMap(identifier -> dependencyRepository.findDependency(identifier)
                                .filter(usage -> characteristicRepository.has(usage, Characteristic.MODEL))
                                .list().stream()
                                .map(useType -> new PackageDependency(identifier, useType))
                                .filter(PackageDependency::notSelfRelation))
                        .distinct()
                        .collect(Collectors.toList());

        PackageIdentifiers allPackages = new PackageIdentifiers(
                modelTypes.list().stream()
                        .map(TypeIdentifier::packageIdentifier)
                        .collect(Collectors.toList()));

        return new PackageDependencies(list, allPackages);
    }

    public void registerDependency(TypeIdentifier typeIdentifier, TypeIdentifiers typeIdentifiers) {
        dependencyRepository.registerDependency(typeIdentifier, typeIdentifiers);
    }
}
