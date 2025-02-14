package org.dddjava.jig.domain.model.information;

import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.term.Glossary;

public interface JigDataProvider {

    MyBatisStatements fetchMybatisStatements();

    EnumModels fetchEnumModels();

    Glossary fetchGlossary();
}
