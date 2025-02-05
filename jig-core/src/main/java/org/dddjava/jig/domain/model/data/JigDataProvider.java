package org.dddjava.jig.domain.model.data;

import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.classes.type.JigTypes;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.term.Glossary;

public interface JigDataProvider {

    MyBatisStatements fetchMybatisStatements();

    EnumModels fetchEnumModels();

    JigTypes fetchJigTypes();

    Glossary fetchTerms();
}
