package org.dddjava.jig.domain.model.data;

import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationsRepository;

import java.util.List;

public interface JigDataProvider {

    static JigDataProvider none() {
        return new JigDataProvider() {
            @Override
            public PersistenceOperationsRepository fetchSqlStatements() {
                return PersistenceOperationsRepository.empty();
            }

            @Override
            public EnumModels fetchEnumModels() {
                return new EnumModels(List.of());
            }
        };
    }

    PersistenceOperationsRepository fetchSqlStatements();

    EnumModels fetchEnumModels();
}
