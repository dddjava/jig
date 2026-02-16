package org.dddjava.jig.infrastructure.javaproductreader;

import org.dddjava.jig.application.JigDataProvider;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;

record DefaultJigDataProvider(JavaSourceModel javaSourceModel,
                              MyBatisStatements myBatisStatements) implements JigDataProvider {

    @Override
    public MyBatisStatements fetchMybatisStatements() {
        return myBatisStatements;
    }

    @Override
    public EnumModels fetchEnumModels() {
        return javaSourceModel().enumModels();
    }
}
