package org.dddjava.jig.infrastructure;

import org.dddjava.jig.domain.model.characteristic.CharacterizedTypeFactory;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropertyCharacterizedTypeFactory implements CharacterizedTypeFactory {

    @Value("${jig.model.pattern:.+\\.domain\\.model\\..+}")
    String modelPattern = ".+\\.domain\\.model\\..+";

    @Value("${jig.repository.pattern:.+Repository}")
    String repositoryPattern = ".+Repository";

    @Override
    public boolean isModel(ByteCode byteCode) {
        return byteCode.typeIdentifier().fullQualifiedName().matches(modelPattern);
    }

    @Override
    public boolean isRepository(ByteCode byteCode) {
        return byteCode.typeIdentifier().fullQualifiedName().matches(repositoryPattern);
    }
}
