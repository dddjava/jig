package jig.application.service;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.identifier.PackageIdentifier;
import jig.domain.model.identifier.PackageIdentifiers;
import jig.domain.model.identifier.TypeIdentifier;
import jig.domain.model.identifier.TypeIdentifiers;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.dependency.PackageDependencies;
import jig.domain.model.relation.dependency.PackageDependency;
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
        TypeIdentifiers modelTypeIdentifiers = characteristicRepository.find(Characteristic.MODEL);
        List<PackageDependency> list =
                modelTypeIdentifiers.list().stream()
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
                modelTypeIdentifiers.list().stream()
                        .map(TypeIdentifier::packageIdentifier)
                        .collect(Collectors.toList()));

        return new PackageDependencies(list, allPackages);
    }
}
