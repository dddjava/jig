package org.dddjava.jig.infrastructure;

import org.dddjava.jig.domain.model.characteristic.CharacterizedTypeFactory;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropertyCharacterizedTypeFactory implements CharacterizedTypeFactory {

    String modelPattern;

    String repositoryPattern;

    public PropertyCharacterizedTypeFactory() {
        this(".+\\.domain\\.model\\..+", ".+Repository");
    }

    @Autowired
    public PropertyCharacterizedTypeFactory(@Value("${jig.model.pattern:.+\\.domain\\.model\\..+}") String modelPattern,
                                            @Value("${jig.repository.pattern:.+Repository}") String repositoryPattern) {
        this.modelPattern = modelPattern;
        this.repositoryPattern = repositoryPattern;
    }


    @Override
    public boolean isModel(TypeByteCode typeByteCode) {
        return typeByteCode.typeIdentifier().fullQualifiedName().matches(modelPattern);
    }

    @Override
    public boolean isRepository(TypeByteCode typeByteCode) {
        return typeByteCode.typeIdentifier().fullQualifiedName().matches(repositoryPattern);
    }
}
