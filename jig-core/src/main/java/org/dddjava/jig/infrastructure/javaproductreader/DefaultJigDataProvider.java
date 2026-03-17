package org.dddjava.jig.infrastructure.javaproductreader;

import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;

record DefaultJigDataProvider(JavaSourceModel javaSourceModel,
                              PersistenceAccessorRepository persistenceAccessorRepository) implements JigDataProvider {

    @Override
    public PersistenceAccessorRepository persistenceAccessorRepository() {
        return persistenceAccessorRepository;
    }

    @Override
    public EnumModels fetchEnumModels() {
        return javaSourceModel().enumModels();
    }
}
