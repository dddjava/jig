package org.dddjava.jig.infrastructure.javaproductreader;

import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorsRepository;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;

record DefaultJigDataProvider(JavaSourceModel javaSourceModel,
                              PersistenceAccessorsRepository persistenceAccessorsRepository) implements JigDataProvider {

    @Override
    public PersistenceAccessorsRepository persistenceAccessorsRepository() {
        return persistenceAccessorsRepository;
    }

    @Override
    public EnumModels fetchEnumModels() {
        return javaSourceModel().enumModels();
    }
}
