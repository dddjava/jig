package jig.application.service;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.dependency.DependencyRepository;
import jig.domain.model.relation.dependency.PackageDependencies;
import org.springframework.stereotype.Service;

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

        return dependencyRepository
                .findAllTypeDependency()
                .toPackageDependenciesWith(modelTypes);
    }

    public void registerDependency(TypeIdentifier typeIdentifier, TypeIdentifiers typeIdentifiers) {
        dependencyRepository.registerDependency(typeIdentifier, typeIdentifiers);
    }
}
