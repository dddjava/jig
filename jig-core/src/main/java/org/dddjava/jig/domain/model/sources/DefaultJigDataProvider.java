package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.application.JigDataProvider;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;

public record DefaultJigDataProvider(JavaSourceModel javaSourceModel,
                                     MyBatisStatements myBatisStatements,
                                     Glossary glossary) implements JigDataProvider {

    @Override
    public MyBatisStatements fetchMybatisStatements() {
        return myBatisStatements;
    }

    @Override
    public EnumModels fetchEnumModels() {
        return javaSourceModel().enumModels();
    }

    @Override
    public Glossary fetchGlossary() {
        return glossary;
    }
}
