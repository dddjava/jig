package org.dddjava.jig.domain.model.data;

import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorsRepository;

import java.util.List;

public interface JigDataProvider {

    static JigDataProvider none() {
        return new JigDataProvider() {
            @Override
            public PersistenceAccessorsRepository persistenceAccessorsRepository() {
                return PersistenceAccessorsRepository.empty();
            }

            @Override
            public EnumModels fetchEnumModels() {
                return new EnumModels(List.of());
            }
        };
    }

    PersistenceAccessorsRepository persistenceAccessorsRepository();

    EnumModels fetchEnumModels();
}
