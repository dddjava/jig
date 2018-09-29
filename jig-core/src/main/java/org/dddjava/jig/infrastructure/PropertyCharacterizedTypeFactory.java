package org.dddjava.jig.infrastructure;

import org.dddjava.jig.domain.model.characteristic.CharacterizedTypeFactory;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.infrastructure.configuration.RepositoryPattern;

public class PropertyCharacterizedTypeFactory implements CharacterizedTypeFactory {


    RepositoryPattern repositoryPattern;

    public PropertyCharacterizedTypeFactory(RepositoryPattern repositoryPattern) {
        this.repositoryPattern = repositoryPattern;
    }

    @Override
    public boolean isRepository(TypeByteCode typeByteCode) {
        return repositoryPattern.matches(typeByteCode);
    }
}
