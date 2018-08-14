package org.dddjava.jig.infrastructure;

import org.dddjava.jig.domain.model.characteristic.CharacterizedTypeFactory;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.infrastructure.configuration.ModelPattern;
import org.dddjava.jig.infrastructure.configuration.RepositoryPattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropertyCharacterizedTypeFactory implements CharacterizedTypeFactory {

    ModelPattern modelPattern;

    RepositoryPattern repositoryPattern;

    public PropertyCharacterizedTypeFactory() {
        this(new ModelPattern(), new RepositoryPattern());
    }

    @Autowired
    public PropertyCharacterizedTypeFactory(@Value("${jig.model.pattern:.+\\.domain\\.model\\..+}") String modelPattern,
                                            @Value("${jig.repository.pattern:.+Repository}") String repositoryPattern) {
        this(new ModelPattern(modelPattern),new RepositoryPattern(repositoryPattern));
    }

    public PropertyCharacterizedTypeFactory(ModelPattern modelPattern, RepositoryPattern repositoryPattern) {
        this.modelPattern = modelPattern;
        this.repositoryPattern = repositoryPattern;
    }

    @Override
    public boolean isModel(TypeByteCode typeByteCode) {
        return modelPattern.matches(typeByteCode);
    }

    @Override
    public boolean isRepository(TypeByteCode typeByteCode) {
        return repositoryPattern.matches(typeByteCode);
    }
}
