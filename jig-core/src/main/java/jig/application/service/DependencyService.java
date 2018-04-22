package jig.application.service;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.identifier.namespace.PackageDepth;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.dependency.DependencyRepository;
import jig.domain.model.relation.dependency.PackageDependencies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DependencyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyService.class);

    private final CharacteristicRepository characteristicRepository;
    private final DependencyRepository dependencyRepository;

    public DependencyService(CharacteristicRepository characteristicRepository, RelationRepository relationRepository) {
        this.characteristicRepository = characteristicRepository;
        this.dependencyRepository = new DependencyRepository();
    }

    public PackageDependencies packageDependencies() {
        TypeIdentifiers modelTypes = characteristicRepository.getTypeIdentifiersOf(Characteristic.MODEL);

        PackageDependencies packageDependencies = dependencyRepository
                .findAllTypeDependency()
                .toPackageDependenciesWith(modelTypes);

        showDepth(packageDependencies);

        return packageDependencies;
    }

    public void registerDependency(TypeIdentifier typeIdentifier, TypeIdentifiers typeIdentifiers) {
        dependencyRepository.registerDependency(typeIdentifier, typeIdentifiers);
    }

    private void showDepth(PackageDependencies packageDependencies) {
        PackageDepth maxDepth = packageDependencies.allPackages().maxDepth();

        LOGGER.info("最大深度: {}", maxDepth.value());
        for (PackageDepth depth : maxDepth.surfaceList()) {
            PackageDependencies dependencies = packageDependencies.applyDepth(depth);
            LOGGER.info("深度 {} の関連数: {} ", depth.value(), dependencies.number().asText());
        }
    }
}
