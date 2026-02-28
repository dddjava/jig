package org.dddjava.jig.infrastructure.javaproductreader;

import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationsRepository;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;

record DefaultJigDataProvider(JavaSourceModel javaSourceModel,
                              PersistenceOperationsRepository persistenceOperationsRepository) implements JigDataProvider {

    @Override
    public PersistenceOperationsRepository fetchSqlStatements() {
        return persistenceOperationsRepository;
    }

    @Override
    public EnumModels fetchEnumModels() {
        return javaSourceModel().enumModels();
    }
}
