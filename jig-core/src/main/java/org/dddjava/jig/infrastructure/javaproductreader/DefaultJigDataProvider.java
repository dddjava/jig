package org.dddjava.jig.infrastructure.javaproductreader;

import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.persistence.SqlStatements;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;

record DefaultJigDataProvider(JavaSourceModel javaSourceModel,
                              SqlStatements sqlStatements) implements JigDataProvider {

    @Override
    public SqlStatements fetchSqlStatements() {
        return sqlStatements;
    }

    @Override
    public EnumModels fetchEnumModels() {
        return javaSourceModel().enumModels();
    }
}
