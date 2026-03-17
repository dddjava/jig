package org.dddjava.jig.domain.model.data;

import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;

import java.util.List;

public interface JigDataProvider {

    static JigDataProvider none() {
        return new JigDataProvider() {
            @Override
            public PersistenceAccessorRepository persistenceAccessorRepository() {
                return PersistenceAccessorRepository.empty();
            }

            @Override
            public EnumModels fetchEnumModels() {
                return new EnumModels(List.of());
            }
        };
    }

    PersistenceAccessorRepository persistenceAccessorRepository();

    EnumModels fetchEnumModels();
}
