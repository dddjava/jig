package jig.application.service;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.identifier.namespace.PackageIdentifier;
import jig.domain.model.identifier.namespace.PackageIdentifiers;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.relation.dependency.PackageDependencies;
import jig.domain.model.relation.dependency.PackageDependency;
import jig.domain.model.specification.Specifications;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DependencyService {

    private final CharacteristicRepository characteristicRepository;
    private final RelationRepository relationRepository;

    public DependencyService(CharacteristicRepository characteristicRepository, RelationRepository relationRepository) {
        this.characteristicRepository = characteristicRepository;
        this.relationRepository = relationRepository;
    }

    public PackageDependencies packageDependencies() {
        TypeIdentifiers modelTypes = characteristicRepository.getTypeIdentifiersOf(Characteristic.MODEL);
        List<PackageDependency> list =
                modelTypes.list().stream()
                        .flatMap(identifier -> {
                            PackageIdentifier packageIdentifier = identifier.packageIdentifier();
                            return relationRepository.findAllUsage(identifier)
                                    .filter(usage -> characteristicRepository.has(usage, Characteristic.MODEL))
                                    .list().stream()
                                    .map(TypeIdentifier::packageIdentifier)
                                    .filter(usagePackage -> !packageIdentifier.equals(usagePackage))
                                    .map(usagePackage -> new PackageDependency(usagePackage, packageIdentifier));
                        })
                        .distinct()
                        .collect(Collectors.toList());

        PackageIdentifiers allPackages = new PackageIdentifiers(
                modelTypes.list().stream()
                        .map(TypeIdentifier::packageIdentifier)
                        .collect(Collectors.toList()));

        return new PackageDependencies(list, allPackages);
    }

    public void register(Specifications specifications) {
        specifications.list().forEach(specification -> {
            Characteristic.register(characteristicRepository, specification);
            RelationType.register(relationRepository, specification);
        });
    }
}
